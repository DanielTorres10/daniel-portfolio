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

// /**
//  * Adds a random greeting to the page.
//  */
// function addRandomGreeting() {
//   const greetings =
//       ['Born and raised in Puerto Rico!', 'Father of four dogs', 'Favorite Movie: Inception', 'Can play percussion.'];

//   // Pick a random greeting.
//   const greeting = greetings[Math.floor(Math.random() * greetings.length)];

//   // Add it to the page.
//   const greetingContainer = document.getElementById('greeting-container');
//   greetingContainer.innerText = greeting;
// }

/**
* week-2 Step 2:
*/

/* Fetch promise */
function getHello(){
  console.log('Fetching message...')
  const promise = fetch('/data');
  promise.then(handlePromise);
} 
/* Get promise content */
function handlePromise(response) {
  console.log('Handling the response.');
  const textPromise = response.text();
  textPromise.then(addQuoteToDom);
}
/* Add the content to html */
function addQuoteToDom(quote) {
  console.log('Adding quote to dom: ' + quote);
  const quoteContainer = document.getElementById('server-container');
  quoteContainer.innerText = quote;
}

/* Other short method */
function getHelloUsingArrowFunctions() {
  fetch('/data').then(response => response.text()).then((quote) => {
    document.getElementById('server-container').innerText = quote;
  });
}
/* Other short method (better?) */
async function getHelloUsingAsyncAwait() {
  console.log('Fetching message...')
  const response = await fetch('/data');
  console.log('Handling the response.');
  const quote = await response.text();
  console.log('Adding quote to dom: ' + quote);
  document.getElementById('server-container').innerText = quote;
}
