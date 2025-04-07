package com.graduationparty.authservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;
import org.mockito.verification.VerificationMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class BaseTest {

  protected void assertEquals(Object expected, Object actual) {
    Assertions.assertEquals(expected, actual);
  }

  protected void assertNotNull(Object actual) {
    Assertions.assertNotNull(actual);
  }

  protected <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
    return Assertions.assertThrows(expectedType, executable);
  }

  protected <T> T mock(Class<T> clazz) {
    return Mockito.mock(clazz);
  }

  protected <T> OngoingStubbing<T> when(T methodCall) {
    return Mockito.when(methodCall);
  }

  protected <T> T verify(T mock, VerificationMode mode) {
    return Mockito.verify(mock, mode);
  }

  protected Stubber doThrow(Throwable toBeThrown) {
    return Mockito.doThrow(toBeThrown);
  }

  protected Stubber doNothing() {
    return Mockito.doNothing();
  }
}
