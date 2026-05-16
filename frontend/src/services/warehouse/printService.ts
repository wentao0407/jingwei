import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface PrintData {
  title?: string | null;
  docNo?: string | null;
  fields?: Record<string, string> | null;
  lines?: Array<Record<string, string>> | null;
}

export async function generateSkuLabelPrint(skuId: string): Promise<PrintData> {
  const response = await apiClient.post('/warehouse/print/sku-label', null, { params: { skuId } });
  return unwrapApiResponse<PrintData>(response.data);
}

export async function generateDocPrint(docType: string, docId: string): Promise<PrintData> {
  const response = await apiClient.post('/warehouse/print/doc', null, {
    params: { docType, docId },
  });
  return unwrapApiResponse<PrintData>(response.data);
}

/**
 * 调用浏览器打印
 */
export function openBrowserPrint(html: string): void {
  const printWindow = window.open('', '_blank');
  if (!printWindow) return;
  printWindow.document.write(html);
  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
}

/**
 * 将 PrintData 渲染为可打印的 HTML
 */
export function renderPrintHtml(data: PrintData): string {
  const fieldRows = Object.entries(data.fields ?? {})
    .map(([label, value]) => `<tr><td class="label">${label}</td><td>${value ?? ''}</td></tr>`)
    .join('\n');

  const headers = data.lines && data.lines.length > 0
    ? Object.keys(data.lines[0])
    : [];
  const thead = headers.length > 0
    ? `<thead><tr>${headers.map((h) => `<th>${h}</th>`).join('')}</tr></thead>`
    : '';
  const tbody = data.lines && data.lines.length > 0
    ? `<tbody>${data.lines.map((row) =>
        `<tr>${headers.map((h) => `<td>${row[h] ?? ''}</td>`).join('')}</tr>`
      ).join('\n')}</tbody>`
    : '';

  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>${data.title ?? '打印'}</title>
  <style>
    body { font-family: "Microsoft YaHei", sans-serif; padding: 20px; }
    h2 { text-align: center; margin-bottom: 16px; }
    .doc-no { text-align: center; color: #666; margin-bottom: 20px; }
    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
    td, th { border: 1px solid #333; padding: 6px 10px; text-align: left; }
    .label { font-weight: bold; width: 120px; background: #f5f5f5; }
    @media print { body { padding: 0; } }
  </style>
</head>
<body>
  <h2>${data.title ?? ''}</h2>
  ${data.docNo ? `<div class="doc-no">编号: ${data.docNo}</div>` : ''}
  ${fieldRows ? `<table>${fieldRows}</table>` : ''}
  ${thead || tbody ? `<table>${thead}${tbody}</table>` : ''}
</body>
</html>`;
}
