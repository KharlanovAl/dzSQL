package ru.Netology.banklogintest.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.Netology.banklogintest.data.DataHelper;
import ru.Netology.banklogintest.data.SqlHelper;
import ru.Netology.banklogintest.page.LoginPage;
import ru.Netology.banklogintest.page.VerificationPage;

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
            var verificationCode = SqlHelper.generateVerificationCode();
            verificationPage.validVerify(verificationCode.getCode());
        }

//
        @Test
        void shouldBlockUserAfterThreeInvalidPasswordAttempts() {
            loginPage.login(authInfo);
            var authInfo = DataHelper.getAuthInfoWithTestData();

            // Три попытки с неверным паролем для пользователя vasya
            for (int i = 0; i < 5; i++) {
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
                    loginPage.verifyErrorNotification("Ошибка! Пользователь заблокирован!");
                }
            }
        }

        @Test
        void shouldGetErrorNotificationIfloginAndRandomVerificationCode() {
            var verificationPage = loginPage.validLogin(authInfo);
            var verificationCode = DataHelper.generateRandomVerificationCode();
            verificationPage.verify(verificationCode.getCode());
            verificationPage.verify("Ошибка! Неверно указан код! Попробуйте еще раз.");
        }

        @Test
        void shouldShowErrorWithInvalidLogin() {
            loginPage.login(authInfo);
            // полностью случайный пользователь
            var invalidAuthInfo = DataHelper.generateRandomUser();
            loginPage.login(invalidAuthInfo);
            // Проверяем сообщение
            loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
        }
}
