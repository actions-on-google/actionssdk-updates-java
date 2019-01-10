# Actions on Google: Updates API sample using Java and Cloud Firestore for Firebase

This sample shows an app that gives tips about developing apps for the Google Assistant using Actions on Google.

## Setup Instructions

### Action configuration
1. Use the Actions on Google Console to add a new project with a name of your choosing and click Create Project.
1. Scroll down and click Actions SDK.
1. Install the gactions CLI if you haven't already.
1. Deploy the fulfillment webhook as described in the Webhook section of this README.
1. Modify the action.json file included in the project, replacing the placeholder fulfillment URL with the URL to your fulfillment.
1. Run the command, adding in your project_id gactions update --action_package action.json --project <YOUR_PROJECT_ID>.
1. Go back to the Actions on Google console and select the project that you have created for this sample.

#### Enable the Actions API
1. Visit the [Google Cloud console](https://console.cloud.google.com/) for the project used in the [Actions console](https://console.actions.google.com).
1. Navigate to the [API Library](https://console.cloud.google.com/apis/library).
1. Search for and enable the Google Actions API.
1. Navigate to the Credentials page in the API manager.
1. Click Create credentials > Service Account Key.
1. Click the Select box under Service Account and click New Service Account.
1. Give the Service Account the name (i.e. "service-account") and the role of Project Owner.
1. Select the JSON key type.
1. Click Create.
1. Place the newly downloaded file in the 'src/main/resources/' directory calling the file `service-account.json`.
1. In the `src/main/resources/config.properties` file of the project, update the value of the `project_id` field with the project ID of your newly created project (no quotes).

#### Setup Cloud Firestore for Firebase
1. Go to the [Firebase console](https://console.firebase.google.com) and select the project that you have created on the Actions on Google console.
1. Click the gear icon, then select *Project settings* > *SERVICE ACCOUNTS*.
1. Generate a new private key and save it in the `src/main/resources/` directory calling the file `firebase-service-account.json`.
1. On the left navigation menu under *DEVELOP*, click on *Database*.
1. Under *Cloud Firestore Beta*, click *Create database*.
1. Select *Start in test mode*, click *Enable*.

#### Configure updates and push notifications
1. Go to the [Actions on Google console](https://console.actions.google.com).
1. Follow the *Console Setup* instructions in the [Daily Updates](https://developers.google.com/actions/assistant/updates/daily) and the [Push Notifications](https://developers.google.com/actions/assistant/updates/notifications) documentation to enable daily updates and push notifications.

### Webhook
The sample includes entry points for both and Google App Engine.

#### Build for Google Cloud Platform
    1. Instructions for [Google Cloud App Engine Standard Environment](https://cloud.google.com/appengine/docs/standard/java/)
    1. Use gcloud CLI to set the project to the name of your Actions project. Use 'gcloud init' to initialize and set your Google cloud project to the name of the Actions project.
    1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle) by running the following command: `gradle appengineDeploy`. You can do this directly from
    IntelliJ by opening the Gradle tray and running the appEngineDeploy task. This will start the process to deploy the fulfillment code to Google Cloud App Engine.

### Test on the Actions on Google simulator
1. Type `Talk to my test app` in the simulator, or say `OK Google, talk to my test app` to any Actions on Google enabled device signed into your developer account.
1. To test daily updates, choose a category. After the tip, the app will show a suggestion chip to subscribe for daily updates. Once a user is subscribed, they will receive update notifications daily for the time they specified.
1. To test push notifications, choose to hear the most recent tip. After the tip, the app will show
a suggestion chip to subscribe for push notifications. To send a push notification to all subscribed users, type "send notification" at any point during the conversation.

For more detailed information on deployment, see the [documentation](https://developers.google.com/actions/dialogflow/deploy-fulfillment).

## References and How to report bugs
* Actions on Google documentation: [https://developers.google.com/actions/](https://developers.google.com/actions/).
* If you find any issues, please open a bug here on GitHub.
* Questions are answered on [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google).

## How to make contributions?
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md).

## License
See [LICENSE](LICENSE).

## Terms
Your use of this sample is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).

## Google+
Actions on Google Developers Community on Google+ [https://g.co/actionsdev](https://g.co/actionsdev).
