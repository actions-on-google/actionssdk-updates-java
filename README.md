# Actions on Google: Updates API Sample 

This sample shows an Action that gives tips about developing Actions for the Google Assistant using Java.

### Setup Instructions

#### Action Configuration
1. From the [Actions on Google Console](https://console.actions.google.com/), add a new project (this will become your *Project ID*) > **Create Project**.
1. Scroll down > select **Actions SDK** > **OK** > **SKIP**
1. [Install the gactions CLI](https://developers.google.com/actions/tools/gactions-cli) if you haven't already.
 
#### Enable the Actions API
1. From [Google Cloud console](https://console.cloud.google.com/) > **Menu ☰** > **APIs & Services** > **Library** > select **Actions API** > **Enable**
    + Make sure to select the correct Project ID from the dropdown
    + To find Project ID: [Actions console](https://console.actions.google.com/) > **Settings** ⚙ 
4. Under **Menu ☰** > **APIs & Services** > **Credentials** > **Create Credentials** > **Service Account Key**.
5. From the dropdown, select **New Service Account**
    + name:  `service-account`
    + role:  **Project/Owner**
    + key type: **JSON** > **Create**
    + Your private JSON file will be downloaded to your local machine
1. Place the newly downloaded file in `src/main/resources/` and rename the file to `service-account.json`
1. In the `src/main/resources/config.properties` file of the project, update the value of the `project_id` field with the project ID of your newly created project
   
#### Firestore Database Configuration
1. From the [Firebase console](https://console.firebase.google.com), find and select your Actions on Google Project ID
1. From **Settings** ⚙ > **Project settings** > *Service accounts** > **Firebase Admin SDK** > **Java** > **Generate new private key**
1. Save private key in the `src/main/resources/` and rename the file to `firebase-service-account.json`
1. In the left navigation menu under **Develop** section > **Database** > **Create database** button > Select **Start in test mode** > **Enable**

#### App Engine Deployment & Webhook Configuration
When a new project is created using the Actions Console, it also creates a Google Cloud project in the background.

1. Download & install the [Google Cloud SDK](https://cloud.google.com/sdk/docs/)
1. Configure the gcloud CLI and set your Google Cloud project to the name of your Actions on Google Project ID, which you can find from the [Actions on Google console](https://console.actions.google.com/) under Settings ⚙
   + `gcloud init`
   + `gcloud auth application-default login`
   + `gcloud components install app-engine-java`
   + `gcloud components update`
1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle):
   + `gradle appengineDeploy` OR
   +  From within IntelliJ, open the Gradle tray and run the appEngineDeploy task.
1. Open the `action.json` file: 
   + In the **conversations object** > replace the placeholder **URL** values with `https://<YOUR_PROJECT_ID>.appspot.com`
1. In terminal, run `gactions update --action_package action.json --project <YOUR_PROJECT_ID>`

#### Configure Daily Updates and Notifications
1. From the [Actions on Google console](https://console.actions.google.com) > under **Build** > **Actions**
1. To setup Daily Updates:
    + Select the `tell_tip` intent > under **User engagement** > **Enable** `Would you like to offer daily updates to users?` > add a title `Daily Advice Alert` > **Save**
    + Select the `tell_most_recent_tip` intent > under **User engagement** > **Enable** `Would you like to send push notifications? If yes, user permission will be needed` > add a title `Most Recent Tip Alert` > **Save**

#### Testing this Sample
1. In the Actions on Google console > from the left navigation menu under **Test**  > **Simulator**.
1. Type `Talk to my test app` in the simulator, or say `OK Google, talk to my test app` to Google Assistant on a mobile device associated with your Action's account.
1. To test daily updates, choose a category. After the tip, the app will show a suggestion chip to subscribe for daily updates. Once a user is subscribed, they will receive update notifications daily for the time they specified.
1. To test push notifications, choose to hear the most recent tip. After the tip, the app will show
   a suggestion chip to subscribe for push notifications. To send a push notification to all subscribed users, type "send notification" at any point during the conversation.

### References & Issues
+ Questions? Go to [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google), [Actions on Google G+ Developer Community](https://g.co/actionsdev), or [Support](https://developers.google.com/actions/support/).
+ For bugs, please report an issue on Github.
+ For Actions on Google [documentation](https://developers.google.com/actions/).
+ For specifics about [Gradle & the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle)
+ For details on deploying [Java apps with App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart)
+ Read more info about [gactions CLI](https://developers.google.com/actions/tools/gactions-cli).

### Make Contributions
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md).

### License
See [LICENSE](LICENSE).

### Terms
Your use of this sample is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).
