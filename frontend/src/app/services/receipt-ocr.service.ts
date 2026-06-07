import { Injectable } from '@angular/core';

export interface ReceiptOcrDraft {
  description: string;
  totalAmount: number | null;
  date: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReceiptOcrService {

  async scanReceipt(file: File): Promise<ReceiptOcrDraft> {
    const { recognize } = await import('tesseract.js');
    const result = await recognize(file, 'eng');
    return this.parseReceiptText(result.data.text);
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
    const candidate = lines.find(line => !this.looksLikeTotalLine(line) && !this.extractAmountFromLine(line));
    return (candidate ?? 'Scanned receipt').slice(0, 120);
  }

  private extractTotalAmount(lines: string[]): number | null {
    const totalLine = [...lines].reverse().find(line => this.looksLikeTotalLine(line) && this.extractAmountFromLine(line) !== null);
    if (totalLine) {
      return this.extractAmountFromLine(totalLine);
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

    const europeanDate = text.match(/\b(0?[1-9]|[12]\d|3[01])[-/.](0?[1-9]|1[0-2])[-/.](20\d{2})\b/);
    if (europeanDate) {
      return this.toIsoDate(Number(europeanDate[3]), Number(europeanDate[2]), Number(europeanDate[1]));
    }

    return this.toLocalIsoDate(new Date());
  }

  private looksLikeTotalLine(line: string): boolean {
    return /\b(total|summe|gesamt|betrag|amount|paid|zahlung)\b/i.test(line);
  }

  private extractAmountFromLine(line: string): number | null {
    return this.extractAmountsFromLine(line).at(-1) ?? null;
  }

  private extractAmountsFromLine(line: string): number[] {
    const matches = line.match(/(?:EUR\s*)?\d{1,6}(?:[.,]\d{2})\b/gi) ?? [];
    return matches
      .map(match => Number(match.replace(/EUR/i, '').trim().replace(',', '.')))
      .filter(amount => Number.isFinite(amount));
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
