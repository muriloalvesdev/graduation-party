package com.graduationparty.authservice;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import software.amazon.awssdk.services.s3.S3Client;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
public abstract class BaseIntegrationTest extends BaseTest {

  @Autowired protected MockMvc mockMvc;
  @MockBean private S3Client s3Client;

  protected ResultActions perform(RequestBuilder requestBuilder) throws Exception {
    return mockMvc.perform(requestBuilder);
  }

  protected ResultActions expectStatus(ResultActions resultActions, int status) throws Exception {
    return resultActions.andExpect(MockMvcResultMatchers.status().is(status));
  }

  protected ResultActions expectOk(ResultActions resultActions) throws Exception {
    return resultActions.andExpect(MockMvcResultMatchers.status().isOk());
  }

  protected ResultActions expectJsonPath(
      ResultActions resultActions, String expression, Matcher<?> matcher) throws Exception {
    return resultActions.andExpect(MockMvcResultMatchers.jsonPath(expression, matcher));
  }

  protected ResultActions expectContent(ResultActions resultActions, String content)
      throws Exception {
    return resultActions.andExpect(MockMvcResultMatchers.content().string(content));
  }
}
