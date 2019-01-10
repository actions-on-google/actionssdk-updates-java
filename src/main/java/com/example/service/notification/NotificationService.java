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
package com.example.service.notification;
import static com.google.common.base.Preconditions.checkNotNull;
import com.example.service.notification.data.Notification;
import com.example.service.notification.data.PushMessage;
import com.example.service.notification.data.PushNotification;
import com.example.service.notification.data.Target;
import com.example.service.tip.data.User;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
public class NotificationService {
  private static final ResourceBundle rb = ResourceBundle.getBundle("config");
  private static final String SERVICE_ACCOUNT_FILE =
      "service-account.json";
  private final ClassLoader classLoader = getClass().getClassLoader();
  private final Firestore db;
  private final GoogleCredentials googleCredentials;
  public NotificationService() throws IOException {
    db = loadDatabase();
    googleCredentials = loadCredentials();
  }
  private Firestore loadDatabase() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      GoogleCredentials credentials =
          GoogleCredentials.getApplicationDefault();
      FirebaseOptions options = new FirebaseOptions.Builder()
          .setCredentials(credentials)
          .setProjectId(rb.getString("project_id"))
          .build();
      FirebaseApp.initializeApp(options);
    }
    return FirestoreClient.getFirestore();
  }
  private ServiceAccountCredentials loadCredentials() throws IOException {
    String actionsApiServiceAccountFile = classLoader
        .getResource(SERVICE_ACCOUNT_FILE)
        .getFile();
    InputStream actionsApiServiceAccount = new FileInputStream(
        actionsApiServiceAccountFile);
    ServiceAccountCredentials serviceAccountCredentials =
        ServiceAccountCredentials.fromStream(actionsApiServiceAccount);
    return (ServiceAccountCredentials)
        serviceAccountCredentials.createScoped(Collections.singleton(
            "https://www.googleapis.com/auth/actions.fulfillment.conversation"));
  }
  private PushNotification createNotification(String title, String userId,
      String intent) {
    Notification notification = new Notification(title);
    Target target = new Target(userId, intent);
    PushMessage message = new PushMessage(notification, target);
    boolean isInSandbox = true;
    return new PushNotification(message, isInSandbox);
  }
  public void sendNotification(String title, String userId, String intent)
      throws IOException {
    checkNotNull(title, "title cannot be null.");
    checkNotNull(userId, "userId cannot be null.");
    checkNotNull(intent, "intent cannot be null.");
    PushNotification notification = createNotification(title, userId, intent);
    HttpPost request = new HttpPost(
        "https://actions.googleapis.com/v2/conversations:send");
    String token = getAccessToken();
    request.setHeader("Content-type", "application/json");
    request.setHeader("Authorization", "Bearer " + token);
    StringEntity entity = new StringEntity(new Gson().toJson(notification));
    entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
    request.setEntity(entity);
    HttpClient httpClient = HttpClientBuilder.create().build();
    httpClient.execute(request);
  }
  private String getAccessToken() throws IOException {
    AccessToken token = googleCredentials.getAccessToken();
    if (googleCredentials.getAccessToken() == null) {
      token = googleCredentials.refreshAccessToken();
    }
    return token.getTokenValue();
  }
  public void subscribeUserToIntent(String userId, String intentName) {
    checkNotNull(userId, "userId cannot be null.");
    checkNotNull(intentName, "intentName cannot be null.");
    CollectionReference collectionReference = db.collection("users");
    Map<String, Object> data = new HashMap<>();
    data.put("intent", intentName);
    data.put("userId", userId);
    collectionReference.add(data);
  }
  public List<User> getSubscribedUsersForIntent(String intentName)
      throws ExecutionException, InterruptedException {
    checkNotNull(intentName, "intentName cannot be null.");
    ApiFuture<QuerySnapshot> query = db.collection("users")
        .whereEqualTo("intent", intentName)
        .get();
    QuerySnapshot querySnapshot = query.get();
    List<User> users = new ArrayList<>();
    querySnapshot.forEach(entry -> {
      String id = entry.getString("userId");
      String intent = entry.getString("intent");
      User user = new User(id, intent);
      users.add(user);
    });
    return users;
  }
}