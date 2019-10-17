# Actions on Google: Daily Updates & Push Notifications Sample

This sample demonstrates Actions on Google user engagement features for use on Google Assistant including push notifications and daily updates -- using the [Actions SDK](https://developers.google.com/assistant/actions/actions-sdk/),
the Actions on Google [Java client library](https://github.com/actions-on-google/actions-on-google-java), and
Google Cloud [App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).

## Setup Instructions
### Prerequisites
1. Download & install the [Google Cloud SDK](https://cloud.google.com/sdk/docs/)
1. [Gradle with App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle)
    + Run `gcloud auth application-default login` with your Gooogle account
    + Install and update the App Engine component,`gcloud components install app-engine-java`
    + Update other components, `gcloud components update`
1.  [Install the gactions CLI](https://developers.google.com/assistant/tools/gactions-cli)
    + You may need to grant execute permission, ‘chmod +x ./gactions’

### Configuration

#### Action Console
1. From the [Actions on Google Console](https://console.actions.google.com/), New project (this will become your *Project ID*) > **Create project**.
1. Scroll down > under **More options** select **Actions SDK** > keep **Use Actions SDK to add Actions** modal open
1. [Install the gactions CLI](https://developers.google.com/assistant/tools/gactions-cli) if you haven't already.

#### Cloud Platform Console
1. From [Google Cloud console](https://console.cloud.google.com/) > select your *Project ID* from the dropdown
1. **Menu ☰** > **APIs & Services** > **Library** > select **Actions API** > **Enable**
1. Under **Menu ☰** > **APIs & Services** > **Credentials** > **Create Credentials** > **Service Account Key**.
1. From the dropdown, select **New Service Account**
    + name:  `service-account`
    + role:  **Project/Owner**
    + key type: **JSON** > **Create**
    + Your private JSON file will be downloaded to your local machine
1. Rename private key file to `service-account.json` and store in the `src/main/resources/` directory.
1. In the `src/main/resources/config.properties` file of the project, update the value of the `project_id` field with the project ID of your newly created project

#### Firestore Database Configuration
1. From the [Firebase console](https://console.firebase.google.com), find and select your Actions on Google *Project ID*
1. In the left navigation menu under **Develop** section > **Database** > **Create database** button > Select **Start in test mode** > **Enable**

#### App Engine Deployment & Webhook Configuration
 1. Configure the gcloud CLI and set your Google Cloud project to the name of your Actions on Google Project ID, which you can find from the [Actions on Google console](https://console.actions.google.com/) under Settings ⚙
   + `gcloud init`
1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle):
   + `gradle appengineDeploy` OR
   +  From within IntelliJ, open the Gradle tray and run the appEngineDeploy task
1. Open the `action.json` file:
   + In the **conversations object** > replace the placeholder **URL** values with `https://<YOUR_PROJECT_ID>.appspot.com`
1. In terminal, run `gactions update --action_package action.json --project <YOUR_PROJECT_ID>`

#### Configure Daily Updates and Notifications
1. Back in the [Actions console](https://console.actions.google.com), from the **Use Actions SDK to add Actions** window > select **OK** from the modal.
1. Under **Build** > **Actions**
    + Select the `Tell a tip` intent > under **User engagement** > **Enable** `Would you like to offer daily updates to users?` > add a title `advice Alert` > **Save**
    + Select the `Tell the most recent tip` intent > under **User engagement** > **Enable** `Would you like to send push notifications? If yes, user permission will be needed` > add a title `Latest Info Alert` > **Save**

#### Running this Sample
1. In the [Actions on Google console](https://console.actions.google.com) > from the top menu click **Test**.
1. Type `Talk to my test app` in the simulator, or say `OK Google, talk to my test app` to Google Assistant on a mobile device associated with your Action's account.
1. To test daily updates, choose a category. After the tip, the app will show a suggestion chip to subscribe for daily updates. Once a user is subscribed, they will receive update notifications daily for the time they specified.
1. To test push notifications, choose to hear the most recent tip. After the tip, the app will show
   a suggestion chip to subscribe for push notifications. To send a push notification to all subscribed users, enter or say "send notification" at any point during the conversation.

### References & Issues
+ Questions? Go to [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google), [Assistant Developer Community on Reddit](https://www.reddit.com/r/GoogleAssistantDev/) or [Support](https://developers.google.com/assistant/support).
+ For bugs, please report an issue on Github.
+ Actions on Google [Documentation](https://developers.google.com/assistant).
+ Getting started with [Actions SDK Guide](https://developers.google.com/assistant/actions/actions-sdk/).
+ More info about [Gradle & the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle).
+ More info about deploying [Java apps with App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).

### Make Contributions
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md).

### License
See [LICENSE](LICENSE).

### Terms
Your use of this sample is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).
