/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.UserKeyManagerData;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.client.RelayApiClient;
import org.symphonyoss.integration.pod.api.client.UserApiClient;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.UserService;

/**
 * Class with unit tests for {@link UserService}
 * Created by cmarcondes on 11/7/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  private static final String SESSION_TOKEN = "95248a7075f53c5458b276d";

  private static final Long USER_ID = 123L;

  private static final String USER_NAME = "symphony";

  private static final String USER_EMAIL = "symphony@symphony.com";

  private static final String CONFIGURATION_ID = "configurationId";

  private static final String KM_TOKEN = "kmToken";

  private static final UserKeyManagerData KM_USER_DATA = new UserKeyManagerData();

  @Mock
  private UserApiClient usersApi;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationBridge integrationBridge;

  @Mock
  private Integration integration;

  @Mock
  private IntegrationSettings integrationSettings;

  @Mock
  private AuthenticationToken tokens;

  @Mock
  private RelayApiClient relayApiClient;

  @Mock
  private LogMessageSource logMessage;

  @InjectMocks
  private UserServiceImpl userService;

  @Before
  public void setup() {
    doReturn(SESSION_TOKEN).when(authenticationProxy).getSessionToken(anyString());
  }

  @Test
  public void testFindUserWithoutEmail() {
    User user = userService.getUserByEmail(null, null);
    assertNull(user);
  }

  @Test
  public void testFindUserByEmailNotFound() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(usersApi).getUserByEmail(anyString(), anyString());

    User user = userService.getUserByEmail(null, USER_EMAIL);
    assertEquals(USER_EMAIL, user.getEmailAddress());
    assertNull(user.getId());
  }

  @Test
  public void testFindUserByEmail() throws RemoteApiException {
    prepareToReturnUser();

    User user = userService.getUserByEmail(null, USER_EMAIL);
    assertEquals(USER_EMAIL, user.getEmailAddress());
    assertEquals("Symphony Display Name", user.getDisplayName());
  }

  @Test
  public void testFindUserByUserName() throws RemoteApiException {
    prepareToReturnUser();

    User user = userService.getUserByUserName(null, USER_NAME);
    assertEquals(USER_NAME, user.getUsername());
    assertEquals("Symphony Display Name", user.getDisplayName());
  }

  @Test
  public void testFindUserByUserId() throws RemoteApiException {
    prepareToReturnUser();

    User user = userService.getUserByUserId(null, null);
    assertNull(user);

    user = userService.getUserByUserId(null, USER_ID);
    assertEquals(USER_ID, user.getId());
    assertEquals("Symphony Display Name", user.getDisplayName());
  }

  @Test
  public void testFindUserByUserIdNotFound() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(usersApi).getUserById(SESSION_TOKEN, USER_ID);

    User user = userService.getUserByUserId(null, USER_ID);
    assertNull(user);
  }

  private void prepareToReturnUser() throws RemoteApiException {
    User user = new User();
    user.setEmailAddress(USER_EMAIL);
    user.setId(USER_ID);
    user.setDisplayName("Symphony Display Name");
    user.setUserName(USER_NAME);

    doReturn(user).when(usersApi).getUserByEmail(SESSION_TOKEN, USER_EMAIL);
    doReturn(user).when(usersApi).getUserByUsername(SESSION_TOKEN, USER_NAME);
    doReturn(user).when(usersApi).getUserById(SESSION_TOKEN, USER_ID);
  }

  @Test
  public void testFindUserByUserNameNotFound() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(usersApi).getUserByUsername(SESSION_TOKEN, USER_NAME);

    User user = userService.getUserByUserName(null, USER_NAME);
    assertEquals(USER_NAME, user.getUsername());
    assertNull(user.getId());
  }

  @Test
  public void testFindUserWithoutUserName() {
    User user = userService.getUserByUserName(null, null);
    assertNull(user);
  }

  @Test
  public void testGetBotUserAccountKeyByConfiguration() {
    String userId = String.valueOf(USER_ID);
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(integrationSettings).when(integration).getSettings();
    doReturn(userId).when(integrationSettings).getType();
    doReturn(tokens).when(authenticationProxy).getToken(userId);
    doReturn(SESSION_TOKEN).when(tokens).getSessionToken();
    doReturn(KM_TOKEN).when(tokens).getKeyManagerToken();
    doReturn(KM_USER_DATA).when(relayApiClient).getUserAccountKeyManagerData(SESSION_TOKEN, KM_TOKEN);

    UserKeyManagerData result = userService.getBotUserAccountKeyData(CONFIGURATION_ID);
    assertEquals(KM_USER_DATA, result);
  }
}
