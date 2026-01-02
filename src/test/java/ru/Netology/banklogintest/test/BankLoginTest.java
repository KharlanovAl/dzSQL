package ru.Netology.banklogintest.test;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.Netology.banklogintest.data.DataHelper;
import ru.Netology.banklogintest.data.SqlHelper;
import ru.Netology.banklogintest.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static ru.Netology.banklogintest.data.SqlHelper.cleanAuthCodes;
import static ru.Netology.banklogintest.data.SqlHelper.cleanDatabase;

public class BankLoginTest {
        LoginPage loginPage;
        DataHelper.AuthInfo authInfo = DataHelper.getAuthInfoWithTestData();

        @AfterAll
        static void tearDownAll() {
            cleanDatabase();
        }

        @AfterEach
        void tearDown() {
            cleanAuthCodes();
        }

        @BeforeEach
        void setUp() {
            loginPage = open("http:/localhost:9999",  LoginPage.class);
        }

        @Test
        void shouldSuccessfulLogin() {
            var verificationPage = loginPage.validLogin(authInfo);
            Selenide.sleep(1000);
            var verificationCode = SqlHelper.generateVerificationCode();
            verificationPage.validVerify(verificationCode.getCode());
        }

        @Test
        void shouldValidateLogin() {
            // Получаем тестовые данные vasya:qwerty123
            var authInfo = DataHelper.getAuthInfoWithTestData();
            var loginPage = new LoginPage();
            var verificationPage = loginPage.validLogin(authInfo);

            // код верификации
            var verificationCode = SqlHelper.generateVerificationCode();

            // Вводим код верификации
            var dashboardPage = verificationPage.validVerify(verificationCode.getCode());

            // Проверяем успешный вход (DashboardPage проверяет наличие текста "Личный кабинет")
        }

        @Test
        void shouldShowErrorWithInvalidVerificationCode() {
            // Получаем тестовые данные vasya:qwerty123
            var authInfo = DataHelper.getAuthInfoWithTestData();
            var loginPage = new LoginPage();
            // Выполняем вход
            var verificationPage = loginPage.validLogin(authInfo);
            // Вводим случайный неверный код верификации
            var invalidCode = DataHelper.generateRandomVerificationCode().getCode();
            verificationPage.verify(invalidCode);

            // Проверяем сообщение об ошибке
            verificationPage.verifyErrorNotification("Ошибка! Неверно указан код!");
        }

        @Test
        void shouldBlockUserAfterThreeInvalidPasswordAttempts() {
            var authInfo = DataHelper.getAuthInfoWithTestData();
            var loginPage = new LoginPage();
            // Три попытки с неверным паролем для пользователя vasya
            for (int i = 0; i < 6; i++) {
                // Создаем AuthInfo с правильным логином vasya, но случайным паролем
                var invalidAuthInfo = new DataHelper.AuthInfo(
                        authInfo.getLogin(),
                        DataHelper.generateRandomPassword()
                );
                loginPage.login(invalidAuthInfo);
                // После каждой попытки проверяем ошибку
                if (i < 2) {
                    // Первые две ошибки - о неверном логине/пароле
                    loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
                } else {
                    // После третьей попытки - пользователь должен быть заблокирован
                    loginPage.verifyErrorNotification("Ошибка! Пользователь заблокирован");
                }
            }
        }

        @Test
        void shouldShowErrorWithInvalidLogin() {
            var loginPage = new LoginPage();
            // полностью случайный пользователь
            var invalidAuthInfo = DataHelper.generateRandomUser();
            loginPage.login(invalidAuthInfo);
            // Проверяем сообщение
            loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
        }
}
