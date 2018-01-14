package com.example.android.testing.unittesting.BasicSample;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class ServiceTest {

  @Mock
  private Service service;

  @Before
  public void setup() {
//        service = new RealServiceImpl();
    when(service.login(eq("test@test.ro"), anyString())).thenReturn(true);
    when(service.login(eq("test@test.com"), anyString())).thenReturn(false);
  }

  @Test
  public void testLogin() {
    boolean login = service.login("test@test.ro", "1234");
    assertThat("testing proper login", login, is(equalTo(true)));
  }

  @Test
  public void testFaildLogin() {
    boolean login = service.login("test@test.com", "1234");
    assertThat("testing failed login", login, is(equalTo(false)));
  }
}
