import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

export interface ReceiptOcrDraft {
  description: string;
  totalAmount: number | null;
  date: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReceiptOcrService {

  async scanCanvas(canvas: HTMLCanvasElement): Promise<ReceiptOcrDraft> {
    const base64 = canvas.toDataURL('image/jpeg', 0.9).split(',')[1];

    const response = await fetch(
      `https://vision.googleapis.com/v1/images:annotate?key=${environment.googleVisionApiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          requests: [{
            image: { content: base64 },
            features: [{ type: 'TEXT_DETECTION', maxResults: 1 }]
          }]
        })
      }
    );

    if (!response.ok) {
      throw new Error(`Vision API error: ${response.status}`);
    }

    const data = await response.json();
    const text: string = data?.responses?.[0]?.textAnnotations?.[0]?.description ?? '';
    return this.parseReceiptText(text);
  }

  parseReceiptText(text: string): ReceiptOcrDraft {
    const lines = text
      .split(/\r?\n/)
      .map(line => line.trim())
      .filter(line => line.length > 0);

    return {
      description: this.extractDescription(lines),
      totalAmount: this.extractTotalAmount(lines),
      date: this.extractDate(text)
    };
  }

  private extractDescription(lines: string[]): string {
    // The merchant title is the first non-total line that contains letters. Pure date and amount
    // lines have no letters, so they are skipped without disqualifying titles like "FR14 MARKET".
    const candidate = lines.find(line => !this.looksLikeTotalLine(line) && /[a-z]/i.test(line));
    return (candidate ?? 'Scanned receipt').slice(0, 120);
  }

  private extractTotalAmount(lines: string[]): number | null {
    const totalLine = [...lines].reverse()
      .find(line => this.looksLikeTotalLine(line) && this.extractAmountsFromLine(line).some(amount => amount > 0));
    if (totalLine) {
      // On a labelled total line the gross total is normally the largest figure on the line
      // (e.g. "NETTO 15,00 BRUTTO 18,90" or "TOTAL 18,90 EUR 0,00 change").
      const totalLineAmounts = this.extractAmountsFromLine(totalLine).filter(amount => amount > 0);
      return Math.max(...totalLineAmounts);
    }

    const amounts = lines
      .flatMap(line => this.extractAmountsFromLine(line))
      .filter(amount => amount > 0);

    if (amounts.length === 0) {
      return null;
    }

    return Math.max(...amounts);
  }

  private extractDate(text: string): string {
    const isoDate = text.match(/\b(20\d{2})[-/.](0?[1-9]|1[0-2])[-/.](0?[1-9]|[12]\d|3[01])\b/);
    if (isoDate) {
      return this.toIsoDate(Number(isoDate[1]), Number(isoDate[2]), Number(isoDate[3]));
    }

    const fullEuropeanDate = text.match(/\b(0?[1-9]|[12]\d|3[01])[-/.](0?[1-9]|1[0-2])[-/.](20\d{2})\b/);
    if (fullEuropeanDate) {
      return this.toIsoDate(Number(fullEuropeanDate[3]), Number(fullEuropeanDate[2]), Number(fullEuropeanDate[1]));
    }

    const shortEuropeanDate = text.match(/\b(0?[1-9]|[12]\d|3[01])[-/.](0?[1-9]|1[0-2])[-/.](\d{2})\b/);
    if (shortEuropeanDate) {
      return this.toIsoDate(2000 + Number(shortEuropeanDate[3]), Number(shortEuropeanDate[2]), Number(shortEuropeanDate[1]));
    }

    return this.toLocalIsoDate(new Date());
  }

  private looksLikeTotalLine(line: string): boolean {
    return /\b(total|summe|gesamt|betrag|amount|paid|zahlung)\b/i.test(line);
  }

  private extractAmountsFromLine(line: string): number[] {
    // Drop date tokens first so "07.06.2026" is not mistaken for the amount 7.06.
    const withoutDates = line.replace(/\b\d{1,4}[.\-/]\d{1,2}[.\-/]\d{2,4}\b/g, ' ');

    // Match grouped amounts ("1.234,56", "1 234,56") and plain amounts ("18", "18,90", "12.40").
    const matches = withoutDates.match(/\d{1,3}(?:[.\s]\d{3})+(?:,\d{1,2})?|\d+(?:[.,]\d{1,2})?/g) ?? [];

    return matches
      .map(match => this.parseAmountToken(match))
      .filter((amount): amount is number => amount !== null && Number.isFinite(amount));
  }

  private parseAmountToken(token: string): number | null {
    let normalized = token.trim();
    const hasDot = normalized.includes('.');
    const hasComma = normalized.includes(',');

    if (hasDot && hasComma) {
      // The separator that appears last is the decimal separator; the other groups thousands.
      if (normalized.lastIndexOf(',') > normalized.lastIndexOf('.')) {
        normalized = normalized.replace(/[.\s]/g, '').replace(',', '.');
      } else {
        normalized = normalized.replace(/[,\s]/g, '');
      }
    } else if (hasComma) {
      const fraction = normalized.split(',').pop() ?? '';
      normalized = (normalized.split(',').length === 2 && fraction.length <= 2)
        ? normalized.replace(',', '.')
        : normalized.replace(/[,\s]/g, '');
    } else if (hasDot) {
      const groups = normalized.split('.');
      const fraction = groups[groups.length - 1];
      if (!(groups.length === 2 && fraction.length <= 2)) {
        // Dots are thousands separators ("1.234", "1.234.567"), not a decimal point.
        normalized = normalized.replace(/\./g, '');
      }
    }

    normalized = normalized.replace(/\s/g, '');
    const value = Number(normalized);
    return Number.isFinite(value) ? value : null;
  }

  private toIsoDate(year: number, month: number, day: number): string {
    const date = new Date(year, month - 1, day);
    if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
      return this.toLocalIsoDate(new Date());
    }
    return this.toLocalIsoDate(date);
  }

  private toLocalIsoDate(date: Date): string {
    const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
  }
}
