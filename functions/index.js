const functions = require('firebase-functions');

exports.imagetotext = functions.https.onRequest((req, res) => {
	// const img = req.query.img;
  // Imports the Google Cloud client libraries
  const vision = require('@google-cloud/vision');

  // Creates a client
  const client = new vision.ImageAnnotatorClient();

  /**
   * TODO(developer): Uncomment the following lines before running the sample.
   */
  // const bucketName = 'Bucket where the file resides, e.g. my-bucket';
  // const fileName = 'Path to file within bucket, e.g. path/to/image.png';

  // Read a remote image as a text document
  client
    .documentTextDetection({ source: { imageUri: 'https://wordart.com/static/img/word_cloud.png' } })
    .then(results => {
      const fullTextAnnotation = results[0].fullTextAnnotation;
      console.log(fullTextAnnotation.text);
      res.status(200).send(`<!doctype html>
        <head>
          <title>Time</title>
        </head>
        <body>
          ${fullTextAnnotation.text}
        </body>
      </html>`);
      return null;
    })
    .catch(err => {
      console.log("error", err);
      res.status(200).send(`<!doctype html>
        <head>
          <title>Time</title>
        </head>
        <body>
          ${err}
        </body>
      </html>`);
    });
	// const json = "{" + "\"requests\": [ { \"image\":{ \"content\": \"" + img +
 //   "\"},\"features\":[{\"type\":\"LABEL_DETECTION\",\"maxResults\":1}]}]}";
	// console.log(JSON.stringify(json));
  // const jsonString = JSON.stringify(json);
  // console.log("word", word);
  // const hours = (new Date().getHours() % 12) + 1 // london is UTC + 1hr;
  // res.status(200).send(`<!doctype html>
  //   <head>
  //     <title>Time</title>
  //   </head>
  //   <body>
  //     ${json}
  //   </body>
  // </html>`);
});

// // Take the text parameter passed to this HTTP endpoint and insert it into the
// // Realtime Database under the path /messages/:pushId/original
// exports.addMessage = functions.https.onRequest((req, res) => {
//   // Grab the text parameter.
//   const original = req.query.text;
//   // Push the new message into the Realtime Database using the Firebase Admin SDK.
//   // admin.database().ref('/messages').push({original: original}).then(snapshot => {
//   //   // Redirect with 303 SEE OTHER to the URL of the pushed object in the Firebase console.
//   //   res.redirect(303, snapshot.ref);
//   // });
//   console.log("message", original);
// });