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

/**
* week-2 Step 5:
*/

/* Loading comments from server and displaying them */
function loadComments() {
  fetch('/data').then(response => response.json()).then((comments) => {
  const commentElement = document.getElementById('history');
  comments.forEach((comment) => {
     console.log('Loading comment: ' + comment);
    commentElement.appendChild(createCommentElement(comment));
    })
  });
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


/* Ignores empty or whitespace comments. */
 function empty() {
  var text;
  text = document.getElementById("text-input").value.trim();
  if (text.length === 0) {
    alert('Unvalid comment.');
    return false;
  }
}

