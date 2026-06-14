package at.ac.tuwien.sepr.groupphase.backend.service;

import org.springframework.stereotype.Component;

/**
 * Renders all outbound WGMS HTML emails with the shared platform palette and layout.
 */
@Component
public class EmailTemplateRenderer {

    private static final String FOREST_GREEN = "#346739";
    private static final String LEAF_GREEN = "#79AE6F";
    private static final String CREAM = "#F2EDC2";
    private static final String EGGWHITE_BG = "#FBFAF3";
    private static final String TEXT_PRIMARY = "#1F2A1F";
    private static final String TEXT_SECONDARY = "#5F6B5F";
    private static final String BORDER_SUBTLE = "rgba(52, 103, 57, 0.16)";

    public String renderNotificationEmail(String recipientFirstName, String headline, String message) {
        String greeting = recipientFirstName == null || recipientFirstName.isBlank()
            ? "Hello,"
            : "Hello " + escapeHtml(recipientFirstName) + ",";

        String body = """
            <p style="margin:0 0 16px;font-size:16px;line-height:26px;color:%s;">%s</p>
            <p style="margin:0 0 8px;font-size:14px;line-height:22px;color:%s;font-weight:600;text-transform:uppercase;letter-spacing:0.04em;">%s</p>
            <div style="margin:0 0 8px;padding:16px 18px;background:%s;border:1px solid %s;border-radius:12px;">
              <p style="margin:0;font-size:16px;line-height:26px;color:%s;">%s</p>
            </div>
            <p style="margin:16px 0 0;font-size:14px;line-height:22px;color:%s;">Open WGMS to review this reminder and take action.</p>
            """.formatted(
            TEXT_PRIMARY, greeting,
            TEXT_SECONDARY, escapeHtml(headline),
            EGGWHITE_BG, BORDER_SUBTLE,
            TEXT_PRIMARY, escapeHtml(message),
            TEXT_SECONDARY
        );

        return renderLayout("WGMS reminder", "A household update needs your attention", body);
    }

    public String renderVerificationEmail(String code) {
        String body = """
            <p style="margin:0 0 16px;font-size:16px;line-height:26px;color:%s;">Use this code to verify your WGMS account:</p>
            <div style="margin:0 0 16px;padding:20px 18px;background:%s;border:1px solid %s;border-radius:12px;text-align:center;">
              <span style="display:inline-block;font-size:32px;line-height:40px;font-weight:700;letter-spacing:8px;color:%s;">%s</span>
            </div>
            <p style="margin:0;font-size:14px;line-height:22px;color:%s;">This code expires in 15 minutes. If you did not request it, you can ignore this email.</p>
            """.formatted(
            TEXT_PRIMARY,
            EGGWHITE_BG, BORDER_SUBTLE,
            FOREST_GREEN, escapeHtml(code),
            TEXT_SECONDARY
        );

        return renderLayout("Verify your email", "Complete your WGMS registration", body);
    }

    private String renderLayout(String eyebrow, String title, String bodyHtml) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>%s</title>
            </head>
            <body style="margin:0;padding:0;background:%s;font-family:Inter,-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:%s;padding:32px 16px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#FFFFFF;border:1px solid %s;border-radius:12px;overflow:hidden;box-shadow:0 12px 32px rgba(31,42,31,0.08);">
                      <tr>
                        <td style="padding:24px 28px;background:%s;color:#FFFFFF;">
                          <p style="margin:0 0 6px;font-size:12px;line-height:18px;font-weight:600;letter-spacing:0.08em;text-transform:uppercase;color:%s;">WGMS</p>
                          <h1 style="margin:0;font-size:28px;line-height:34px;font-weight:700;color:#FFFFFF;">%s</h1>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:28px;">
                          <p style="margin:0 0 18px;font-size:14px;line-height:20px;color:%s;">%s</p>
                          %s
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:18px 28px 24px;border-top:1px solid %s;background:%s;">
                          <p style="margin:0;font-size:13px;line-height:20px;color:%s;">Warm regards,<br><strong style="color:%s;">The WGMS Team</strong></p>
                        </td>
                      </tr>
                    </table>
                    <p style="margin:16px 0 0;font-size:12px;line-height:18px;color:%s;">Shared household management for your WG</p>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
            escapeHtml(title),
            EGGWHITE_BG,
            EGGWHITE_BG,
            BORDER_SUBTLE,
            FOREST_GREEN,
            CREAM,
            escapeHtml(title),
            TEXT_SECONDARY,
            escapeHtml(eyebrow),
            bodyHtml,
            BORDER_SUBTLE,
            EGGWHITE_BG,
            TEXT_SECONDARY,
            TEXT_PRIMARY,
            TEXT_SECONDARY
        );
    }

    static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
