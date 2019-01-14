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
package com.example.service.tip;

import static com.google.common.base.Preconditions.checkNotNull;

import com.example.service.tip.data.Tip;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TipService {
  private static final ResourceBundle rb = ResourceBundle.getBundle("config");
  private final ClassLoader classLoader = getClass().getClassLoader();
  private final Firestore db;

  public TipService() throws IOException {
    db = loadDatabase();
  }

  private Firestore loadDatabase() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
      FirebaseOptions options =
          new FirebaseOptions.Builder()
              .setCredentials(credentials)
              .setProjectId(rb.getString("project_id"))
              .build();
      FirebaseApp.initializeApp(options);
    }
    return FirestoreClient.getFirestore();
  }

  private void clearTips() throws ExecutionException, InterruptedException {
    WriteBatch batch = db.batch();
    ApiFuture<QuerySnapshot> query = db.collection("tips").get();
    QuerySnapshot querySnapshot = query.get();
    querySnapshot.getDocuments().forEach(tip -> batch.delete(tip.getReference()));
    batch.commit().get();
  }

  private void addTips(List<Tip> tips) throws ExecutionException, InterruptedException {
    WriteBatch batch = db.batch();
    CollectionReference tipsRef = db.collection("tips");
    tips.forEach(tip -> batch.set(tipsRef.document(), tip));
    batch.commit().get();
  }

  public void loadTipsFromFile(String fileName)
      throws ExecutionException, InterruptedException, FileNotFoundException {
    checkNotNull(fileName, "fileName cannot be null.");
    String tipsFile = classLoader.getResource(fileName).getFile();
    List<Tip> tips =
        new Gson().fromJson(new FileReader(tipsFile), new TypeToken<List<Tip>>() {}.getType());
    clearTips();
    addTips(tips);
  }

  public List<String> getCategories() throws ExecutionException, InterruptedException {
    ApiFuture<QuerySnapshot> query = db.collection("tips").get();
    QuerySnapshot querySnapshot = query.get();
    List<String> uniqueCategories =
        querySnapshot
            .getDocuments()
            .stream()
            .map(currentValue -> currentValue.getString("category"))
            .distinct()
            .collect(Collectors.toList());
    return uniqueCategories;
  }

  private Tip createTip(QueryDocumentSnapshot tipSnapshot) {
    String category = tipSnapshot.getString("tip");
    String description = tipSnapshot.getString("tip");
    String url = tipSnapshot.getString("url");
    String createdAt = tipSnapshot.getString("createdAt");
    return new Tip(category, description, url, createdAt);
  }

  public Tip getMostRecentTip() throws ExecutionException, InterruptedException {
    CollectionReference tipsRef = db.collection("tips");
    ApiFuture<QuerySnapshot> query =
        tipsRef.orderBy("createdAt", Query.Direction.DESCENDING).limit(1).get();
    QueryDocumentSnapshot tipSnapshot = query.get().getDocuments().get(0);
    return createTip(tipSnapshot);
  }

  public Tip getRandomTip(String category) throws ExecutionException, InterruptedException {
    checkNotNull(category, "category cannot be null.");
    CollectionReference tipsRef = db.collection("tips");
    ApiFuture<QuerySnapshot> query;
    if (!category.equals("random")) {
      query = tipsRef.whereEqualTo("category", category).get();
    } else {
      query = tipsRef.get();
    }
    // Retrieve a random data from the list
    QuerySnapshot querySnapshot = query.get();
    List<QueryDocumentSnapshot> tips = querySnapshot.getDocuments();
    int tipIndex = (int) (Math.random() * tips.size());
    QueryDocumentSnapshot tipSnapshot = tips.get(tipIndex);
    return createTip(tipSnapshot);
  }
}
