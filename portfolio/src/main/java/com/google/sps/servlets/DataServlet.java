// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import com.google.sps.data.Comment;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import com.google.gson.Gson;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.PrintWriter;

/* Servlet that handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get comments from Datastore
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Add comment properties in an ArrayList
    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String text = (String) entity.getProperty("text-input");
      long timestamp = (long) entity.getProperty("timestamp");
      double score = (double) entity.getProperty("sentiment_score");
      String user = (String) entity.getProperty("user");

      comments.add(new Comment(id, text, timestamp, score, user));
    }
    // Converts to JSON and responds
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(comments));
  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the input from the form in html page
    String text = getComment(request, "text-input");
    long timestamp = System.currentTimeMillis();

    UserService userService = UserServiceFactory.getUserService();

    String user = getUserNickname(userService.getCurrentUser().getUserId());
    
    // Create new Entity for Datastore
    Entity comEntity = new Entity("Comment");
    comEntity.setProperty("text-input", text);
    comEntity.setProperty("timestamp", timestamp);
  
    // Sentiment analizer for comments
    Document doc =
    Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();

    comEntity.setProperty("sentiment_score", score);
    comEntity.setProperty("user", user);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(comEntity);
    
    // Redirect back to the HTML page.
    response.sendRedirect("/about/about.html");
  }


  /* @return the request parameter and trims whitespace */
  private String getComment(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    return value.trim();
  }

/* Returns the nickname of the user with id, or null if the user has not set a nickname */
  private String getUserNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
      new Query("UserInfo")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }
}
