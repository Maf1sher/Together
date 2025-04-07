package org.mafisher.togetherbackend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.handler.BusinessErrorCodes;
import org.mafisher.togetherbackend.handler.CustomException;
import org.mafisher.togetherbackend.service.impl.CookieServiceImpl;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CookieServiceTest {

    private final CookieService cookieService = new CookieServiceImpl();

    @Test
    void getNewCookie_ShouldCreateValidCookie() {
        String name = "testName";
        String value = "testValue";

        Cookie cookie = cookieService.getNewCookie(name, value);

        assertEquals(name, cookie.getName());
        assertEquals(value, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(24 * 60 * 60, cookie.getMaxAge());
    }

    @Test
    void deleteCookie_ShouldInvalidateCookie() {
        String name = "testCookie";

        Cookie cookie = cookieService.deleteCookie(name);

        assertEquals(name, cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void getJwtCookie_WhenCookieExists_ShouldReturnToken() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie jwtCookie = new Cookie("jwt", "testToken");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});

        String result = cookieService.getJwtCookie(request);

        assertEquals("testToken", result);
    }

    @Test
    void getJwtCookie_WhenNoCookies_ShouldThrowException() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        assertThrows(CustomException.class, () -> cookieService.getJwtCookie(request));
    }

    @Test
    void getJwtCookie_WhenJwtCookieMissing_ShouldThrowException() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie otherCookie = new Cookie("other", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});

        assertThrows(CustomException.class, () -> cookieService.getJwtCookie(request));
    }

    @Test
    void getJwtCookie_WhenMultipleCookies_ShouldReturnJwt() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie cookie1 = new Cookie("test", "val");
        Cookie jwtCookie = new Cookie("jwt", "correctToken");
        Cookie cookie2 = new Cookie("another", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie1, jwtCookie, cookie2});

        String result = cookieService.getJwtCookie(request);

        assertEquals("correctToken", result);
    }

    @Test
    void getJwtCookie_WhenJwtCookieIsCaseSensitive_ShouldThrowException() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie invalidCaseCookie = new Cookie("JWT", "token");
        when(request.getCookies()).thenReturn(new Cookie[]{invalidCaseCookie});

        assertThrows(CustomException.class, () -> cookieService.getJwtCookie(request));
    }

    @Test
    void getJwtCookie_ShouldThrowWithCorrectErrorCode() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> cookieService.getJwtCookie(request)
        );

        assertEquals(BusinessErrorCodes.BAD_COOKIE, exception.getErrorCode());
    }
}
