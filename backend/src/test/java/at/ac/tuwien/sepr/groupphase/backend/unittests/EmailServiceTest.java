package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.MockEmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ResendEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailServiceTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            MockEmailService.class,
            ResendEmailService.class
        ));

    @Test
    public void whenProviderIsMock_thenMockEmailServiceIsActive() {
        this.contextRunner
            .withPropertyValues("wgms.mail.provider=mock")
            .run(context -> {
                assertThat(context).hasSingleBean(EmailService.class);
                assertThat(context.getBean(EmailService.class)).isInstanceOf(MockEmailService.class);
            });
    }

    @Test
    public void whenProviderIsResend_thenResendEmailServiceIsActive() {
        this.contextRunner
            .withPropertyValues("wgms.mail.provider=resend")
            .run(context -> {
                assertThat(context).hasSingleBean(EmailService.class);
                assertThat(context.getBean(EmailService.class)).isInstanceOf(ResendEmailService.class);
            });
    }

    @Test
    public void whenProviderIsNotSet_thenMockEmailServiceIsDefault() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(EmailService.class);
                assertThat(context.getBean(EmailService.class)).isInstanceOf(MockEmailService.class);
            });
    }
}
