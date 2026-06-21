import { AfterViewInit, Component, ElementRef, NgZone, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import jsQR from 'jsqr';
import { ShoppingItemService } from '../../services/shopping-item.service';

type ScanState = 'idle' | 'scanning' | 'submitting' | 'success' | 'error';

/**
 * FR-18 — QR scanner page for one-click shopping item entry (UC-04).
 *
 * Uses {@code getUserMedia} for the camera and {@code jsQR} to decode frames off
 * a hidden canvas. On the first successful decode the raw payload is sent to the
 * backend, which is the sole validator (contract 9.5); the client never trusts
 * the decoded text. Because cameras are inconsistent across devices (risk R-13),
 * a manual fallback is always offered: paste a code, or jump to the normal
 * "add item" form.
 *
 * The decode loop runs outside the Angular zone to avoid a change-detection pass
 * on every animation frame, re-entering the zone only when something happens.
 */
@Component({
  selector: 'app-shopping-item-scan',
  templateUrl: './shopping-item-scan.component.html',
  styleUrls: ['./shopping-item-scan.component.scss'],
  standalone: false
})
export class ShoppingItemScanComponent implements AfterViewInit, OnDestroy {

  @ViewChild('video') videoRef?: ElementRef<HTMLVideoElement>;
  @ViewChild('canvas') canvasRef?: ElementRef<HTMLCanvasElement>;

  state: ScanState = 'idle';
  cameraActive = false;
  cameraError: string | null = null;
  errorMessage: string | null = null;
  createdName: string | null = null;
  showManual = false;

  private stream: MediaStream | null = null;
  private rafId: number | null = null;
  private destroyed = false;

  constructor(
    private shoppingItemService: ShoppingItemService,
    private router: Router,
    private zone: NgZone
  ) { }

  ngAfterViewInit(): void {
    this.startCamera();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.stopCamera();
  }

  /** Requests the camera and, on success, starts the decode loop. */
  async startCamera(): Promise<void> {
    this.cameraError = null;
    const mediaDevices: MediaDevices | undefined =
      typeof navigator !== 'undefined' ? navigator.mediaDevices : undefined;

    if (!mediaDevices || typeof mediaDevices.getUserMedia !== 'function') {
      this.cameraError = 'No camera is available here. You can paste a code or add the item manually.';
      this.showManual = true;
      return;
    }

    try {
      this.stream = await mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' } },
        audio: false
      });
      const video = this.videoRef?.nativeElement;
      if (!video || this.destroyed) {
        this.stopCamera();
        return;
      }
      video.srcObject = this.stream;
      video.setAttribute('playsinline', 'true'); // inline playback on iOS Safari
      await video.play();
      this.cameraActive = true;
      this.state = 'scanning';
      this.zone.runOutsideAngular(() => this.scanLoop());
    } catch {
      this.cameraError = 'Camera access was blocked. Allow camera permission, or paste a code below.';
      this.showManual = true;
    }
  }

  private scanLoop(): void {
    if (this.destroyed || this.state !== 'scanning') {
      return;
    }
    const video = this.videoRef?.nativeElement;
    const canvas = this.canvasRef?.nativeElement;

    if (video && canvas && video.readyState === video.HAVE_ENOUGH_DATA) {
      const width = video.videoWidth;
      const height = video.videoHeight;
      if (width && height) {
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d', { willReadFrequently: true });
        if (ctx) {
          ctx.drawImage(video, 0, 0, width, height);
          const imageData = ctx.getImageData(0, 0, width, height);
          const code = jsQR(imageData.data, width, height, { inversionAttempts: 'dontInvert' });
          if (code && code.data) {
            this.zone.run(() => this.onDecoded(code.data));
            return;
          }
        }
      }
    }
    this.rafId = requestAnimationFrame(() => this.scanLoop());
  }

  private onDecoded(payload: string): void {
    // Lock immediately so the same code isn't submitted on every frame.
    this.state = 'submitting';
    this.stopCamera();
    this.submitPayload(payload);
  }

  /** Manual fallback: submit a pasted payload (value read straight off the textarea). */
  submitManual(rawPayload: string): void {
    const payload = (rawPayload ?? '').trim();
    if (!payload || this.state === 'submitting') {
      return;
    }
    this.state = 'submitting';
    this.submitPayload(payload);
  }

  private submitPayload(payload: string): void {
    this.errorMessage = null;
    this.shoppingItemService.scan(payload).subscribe({
      next: (item) => {
        this.createdName = item.name;
        this.state = 'success';
        // Short confirmation, then back to the list where the new item appears.
        setTimeout(() => this.router.navigate(['/shopping-items']), 900);
      },
      error: (err) => {
        this.state = 'error';
        this.errorMessage = this.extractError(err);
      }
    });
  }

  /** Resets after a failed attempt and restarts the camera. */
  rescan(): void {
    this.errorMessage = null;
    this.createdName = null;
    this.state = 'idle';
    this.cameraActive = false;
    this.startCamera();
  }

  toggleManual(): void {
    this.showManual = !this.showManual;
  }

  private stopCamera(): void {
    if (this.rafId !== null) {
      cancelAnimationFrame(this.rafId);
      this.rafId = null;
    }
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }
    this.cameraActive = false;
  }

  private extractError(err: { status?: number; error?: { message?: string } }): string {
    if (err?.error?.message) {
      return err.error.message;
    }
    if (err?.status === 400) {
      return 'That QR code is not a valid WGMS shopping item.';
    }
    if (err?.status === 403) {
      return 'You need to be part of a household before adding shopping items.';
    }
    if (err?.status === 401) {
      return 'Your session has expired. Please log in again.';
    }
    return 'Could not add the scanned item. Please try again.';
  }
}
