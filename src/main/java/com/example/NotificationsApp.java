/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import static com.google.common.base.Preconditions.checkNotNull;

import com.example.service.notification.NotificationService;
import com.example.service.tip.TipService;
import com.example.service.tip.data.Tip;
import com.example.service.tip.data.User;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.ActionsSdkApp;
import com.google.actions.api.Capability;
import com.google.actions.api.ConstantsKt;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.RegisterUpdate;
import com.google.actions.api.response.helperintent.UpdatePermission;
import com.google.api.services.actions_fulfillment.v2.model.Argument;
import com.google.api.services.actions_fulfillment.v2.model.BasicCard;
import com.google.api.services.actions_fulfillment.v2.model.Button;
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction;
import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class NotificationsApp extends ActionsSdkApp {

  private static final String TIPS_FILE_NAME = "tips.json";
  private static final String BUNDLE_NAME = "resources";
  private final TipService tipService;
  private final NotificationService notificationService;
  private final ResourceBundle prompts;

  public NotificationsApp() throws IOException, ExecutionException, InterruptedException {
    this(new TipService(), new NotificationService(), ResourceBundle.getBundle(BUNDLE_NAME));
  }

  protected NotificationsApp(
      TipService tipService, NotificationService notificationService, ResourceBundle resourceBundle)
      throws IOException, ExecutionException, InterruptedException {
    super();
    this.tipService = checkNotNull(tipService, "tipService cannot be null.");
    this.notificationService =
        checkNotNull(notificationService, "notificationService cannot be null.");
    this.prompts = checkNotNull(resourceBundle, "resourceBundle cannot be null.");
    tipService.loadTipsFromFile(TIPS_FILE_NAME);
  }

  @ForIntent("actions.intent.MAIN")
  public ActionResponse welcome(ActionRequest request)
      throws ExecutionException, InterruptedException {
    ResponseBuilder responseBuilder = getResponseBuilder(request);

    if (request.hasCapability(Capability.SCREEN_OUTPUT.getValue())) {
      responseBuilder.add(prompts.getString("welcome"));

      // Get a list of all data categories from the database
      List<String> uniqueCategories = tipService.getCategories();
      uniqueCategories.add(prompts.getString("latestTipSuggestion"));
      uniqueCategories = Lists.reverse(uniqueCategories);
      responseBuilder.addSuggestions(uniqueCategories.toArray(new String[0]));
    } else {
      // User engagement features aren't currently supported on speaker-only devices
      // See docs: https://developers.google.com/actions/assistant/updates/overview
      responseBuilder.add(prompts.getString("welcomeSpeakerOnly"));
      responseBuilder.endConversation();
    }
    return responseBuilder.build();
  }

  @ForIntent("actions.intent.TEXT")
  public ActionResponse handleRawinput(ActionRequest request)
      throws ExecutionException, InterruptedException, FileNotFoundException {
    String rawText = request.getRawInput().getQuery();
    System.out.println("TEXT intent: user said - " + rawText);
    List<String> categories = tipService.getCategories();

    // List of strings against which to compare user's utterance
    String notificationsSuggestion = prompts.getString("notificationsSuggestion");
    String dailyUpdatesSuggestion = prompts.getString("dailyUpdatesSuggestion");
    String latestTipSuggestion = prompts.getString("latestTipSuggestion");

    if (rawText.equals(notificationsSuggestion)) {
      return setupNotification(request);
    } else if (rawText.equals(dailyUpdatesSuggestion)) {
      return setupDailyUpdates(request);
    } else if (rawText.equals(latestTipSuggestion)) {
      return tellMostRecentTip(request);
    } else if (rawText.toLowerCase().equals("restore tips")) {
      return restoreTips(request);
    } else if (rawText.toLowerCase().equals("send notification")) {
      return sendNotification(request);
    } else {
      for (int i = 0; i < categories.size(); i++) {
        String category = categories.get(i);
        if (rawText.toLowerCase().contains(category)) {
          return tellTip(request, category);
        }
      }
    }

    // User has said something unrecognized
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder.add(prompts.getString("fallback"));
    responseBuilder.addSuggestions(
        new String[] {notificationsSuggestion, dailyUpdatesSuggestion, latestTipSuggestion});

    return responseBuilder.build();
  }

  @ForIntent("tell.tip")
  public ActionResponse tellTip(ActionRequest request, String category)
      throws ExecutionException, InterruptedException {
    Tip tip = tipService.getRandomTip(category);
    // Send data to the user
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder.add(tip.getTip());
    // Display data in a card
    responseBuilder
        .add(new BasicCard()
              .setFormattedText(tip.getTip())
              .setButtons(Arrays.asList(new Button()
                              .setTitle(prompts.getString("buttonTitle"))
                              .setOpenUrlAction(new OpenUrlAction().setUrl(tip.getUrl())))))
        .addSuggestions(new String[] {prompts.getString("dailyUpdatesSuggestion")});

    return responseBuilder.build();
  }

  @ForIntent("tell.most.recent.tip")
  public ActionResponse tellMostRecentTip(ActionRequest request)
      throws ExecutionException, InterruptedException {
    // Retrieve the most recently added data from the database
    Tip tip = tipService.getMostRecentTip();
    // Send data to the user
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder.add(tip.getTip());
    // Display data in a card for devices
    responseBuilder
        .add(new BasicCard()
              .setFormattedText(tip.getTip())
              .setButtons(Arrays.asList(new Button()
                              .setTitle(prompts.getString("buttonTitle"))
                              .setOpenUrlAction(new OpenUrlAction().setUrl(tip.getUrl())))))
        .addSuggestions(new String[] {prompts.getString("notificationsSuggestion")});

    return responseBuilder.build();
  }

  @ForIntent("actions.intent.PERMISSION")
  public ActionResponse completeNotificationSetup(ActionRequest request) {
    // Verify the user has subscribed for push notifications
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    if (request.isPermissionGranted()) {
      Argument userId = request.getArgument(ConstantsKt.ARG_UPDATES_USER_ID);
      if (userId != null) {
        // Store the user's ID in the database
        notificationService.subscribeUserToIntent(userId.getTextValue(), "tell.most.recent.tip");
      }
      responseBuilder.add(prompts.getString("notificationSetupSuccess"));
    } else {
      responseBuilder.add(prompts.getString("notificationSetupFail"));
    }
    responseBuilder.endConversation();
    return responseBuilder.build();
  }

  @ForIntent("actions.intent.REGISTER_UPDATE")
  public ActionResponse completeDailyUpdatesSetup(ActionRequest request) {
    // Verify the user has subscribed for daily updates
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    if (request.isUpdateRegistered()) {
      responseBuilder.add(prompts.getString("dailyUpdateSetupSuccess"));
    } else {
      responseBuilder.add(prompts.getString("dailyUpdateSetupFail"));
    }
    responseBuilder.endConversation();

    return responseBuilder.build();
  }

  private ActionResponse setupDailyUpdates(ActionRequest request) {
    // Ask for the user's permission to send daily updates
    String category = (String) request.getParameter("category");
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder.add(
        new RegisterUpdate()
            .setIntent("tell.tip")
            .setFrequency("DAILY")
            .setArguments(
                Arrays.asList(new Argument().setName("category").setTextValue(category))));

    return responseBuilder.build();
  }

  private ActionResponse sendNotification(ActionRequest request)
      throws ExecutionException, InterruptedException {
    // Retrieve a list of users that have subscribed for push notifications
    List<User> users = notificationService.getSubscribedUsersForIntent("tell.most.recent.tip");
    // Send a push notification to every user
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    users.forEach(
        user -> {
          String title = prompts.getString("notificationTitle");
          try {
            notificationService.sendNotification(title, user.getId(), user.getIntent());
            responseBuilder.add(prompts.getString("notificationSendSuccess"));
          } catch (IOException e) {
            e.printStackTrace();
            responseBuilder.add(prompts.getString("notificationSendFail"));
          }
        });

    return responseBuilder.build();
  }

  private ActionResponse setupNotification(ActionRequest request) {
    // Ask for the user's permission to send push notifications
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder
        .add("Placeholder text")
        .add(new UpdatePermission().setIntent("tell.most.recent.tip"));

    return responseBuilder.build();
  }

  private ActionResponse restoreTips(ActionRequest request)
      throws ExecutionException, InterruptedException, FileNotFoundException {
    tipService.loadTipsFromFile(TIPS_FILE_NAME);
    ResponseBuilder responseBuilder = getResponseBuilder(request);
    responseBuilder.add(prompts.getString("restoreTips"));

    return responseBuilder.build();
  }
}
