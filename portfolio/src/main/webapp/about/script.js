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
* week-2 Step 3:
*/

/* Alternative method */
async function getCommentUsingAsyncAwait() {
  const response = await fetch('/data');
  const quote = await response.text();

  console.log('Adding JSON: ' + quote);
  const helloPElement = document.getElementById('history');
  helloPElement.innerHTML = '';
  helloPElement.appendChild(
      createPElement(quote));
 }


/* Creates an <p> element containing text. */
function createPElement(text) {
  const pElement = document.createElement('p');
  pElement.innerText = text;
  return pElement;
}
/* Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
