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


// Comment input
const input = document.getElementById('text-input');
// Login/Logout button
const login = document.getElementById('login');
const commentHistory = document.getElementById('history');

/* Loading comments from server and displaying them */
async function loadComments() {
  const response = await fetch('/data'); 
  const comments = await response.json();
  
  comments.forEach((comment) => {
    commentHistory.appendChild(createCommentElement(comment));
    if (comment.sentiment_score < 0) 
    console.log('Negative comment: '+ comment.text + '\n' +  'Sentimental Score: ' + comment.sentiment_score);
    else if(comment.sentiment_score > 0) 
    console.log('Positive comment: '+ comment.text + '\n' +  'Sentimental Score: ' + comment.sentiment_score);
    else 
    console.log('Neutral comment: '+ comment.text + '\n' +  'Sentimental Score: ' + comment.sentiment_score);
    console.log('Comment posted by: ' + comment.user);
  })
}

/* Creates an element that represents a comment */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';

  const PElement = document.createElement('p');
  PElement.innerText = comment.text;

  commentElement.appendChild(PElement);
  return commentElement;
}


/* Ignores empty or whitespace comments */
function empty() {
  if (input.value.trim().length === 0) {
    alert('Unvalid comment.');
    return false;
  }
}

async function checkIfUserIsLoggedIn() {
  const response = await fetch('/home'); 
  const URL = await response.text();
  // Only display form is user is logged in
  const ref = document.getElementById('reference');
  const form = document.getElementById('showOnLogin');
  console.log(URL);
  if (ref.getAttribute("href") === "") ref.href = URL;
  if (URL.includes("logout")) {
    ref.innerHTML = "logout";
    form.style.display = "inline";
  }
  else {
    document.getElementById('comment-intro').innerHTML = 'Login to share your thoughts about me!'  
    ref.innerHTML = "login";
    form.style.display = "none";
  }
}

var clicks = 0;
function displayComments(){
  clicks +=1;
  if (commentHistory.style.display === "none" || clicks == 1) {
    commentHistory.style.display = "block";
  } else {
    commentHistory.style.display = "none";
  }
}

