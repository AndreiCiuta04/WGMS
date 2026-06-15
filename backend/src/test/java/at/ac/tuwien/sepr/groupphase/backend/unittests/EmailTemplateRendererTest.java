package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.service.EmailTemplateRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailTemplateRendererTest {

    private EmailTemplateRenderer renderer;

    @BeforeEach
    public void setUp() {
        renderer = new EmailTemplateRenderer();
    }

    @Test
    public void givenNotificationEmail_whenRendered_thenUsesWgmsBrandColorsAndEscapesMessage() {
        String html = renderer.renderNotificationEmail("Anna", "Task assigned", "You have been assigned <script>alert(1)</script>");

        assertTrue(html.contains("#346739"));
        assertTrue(html.contains("#FBFAF3"));
        assertTrue(html.contains("Hello Anna,"));
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertFalse(html.contains("<script>alert(1)</script>"));
    }

    @Test
    public void givenVerificationEmail_whenRendered_thenUsesStyledCodeBlock() {
        String html = renderer.renderVerificationEmail("123456");

        assertTrue(html.contains("123456"));
        assertTrue(html.contains("#346739"));
        assertTrue(html.contains("Verify your email"));
        assertTrue(html.contains("expires in 15 minutes"));
    }
}
