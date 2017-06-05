package com.foodonet.foodonet.commonMethods;

import android.content.Context;
import android.content.Intent;

import com.foodonet.foodonet.R;
import com.foodonet.foodonet.activities.AboutUsActivity;
import com.foodonet.foodonet.activities.GroupsActivity;
import com.foodonet.foodonet.activities.MainActivity;
import com.foodonet.foodonet.activities.MapActivity;
import com.foodonet.foodonet.activities.NotificationActivity;
import com.foodonet.foodonet.activities.PrefsActivity;
import com.foodonet.foodonet.activities.PublicationActivity;
import com.foodonet.foodonet.activities.SignInActivity;
import com.foodonet.foodonet.dialogs.ContactUsDialog;

public class DrawerNavigation {

    public static void navigationItemSelectedAction(Context context, int id) {
        // handles the navigation actions from the drawer
        Intent intent;
        switch (id) {
            case R.id.nav_my_shares:
                intent = new Intent(context, PublicationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION, PublicationActivity.MY_PUBLICATIONS_TAG);
                context.startActivity(intent);
                break;
            case R.id.nav_all_events:
                intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            case R.id.nav_map_view:
                intent = new Intent(context, MapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            case R.id.nav_notifications:
                intent = new Intent(context, NotificationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            case R.id.nav_groups:
                intent = new Intent(context, GroupsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            case R.id.nav_settings:
                if (!CommonMethods.isUserSignedIn(context)) {
                    // if the user is not signed in yet, open the sign in activity
                    intent = new Intent(context, SignInActivity.class);
                } else {
                    // open the preferences fragment activity
                    intent = new Intent(context, PrefsActivity.class);
                }
                context.startActivity(intent);
                break;
            case R.id.nav_contact_us:
                ContactUsDialog dialog = new ContactUsDialog(context);
                dialog.show();
                break;
            case R.id.nav_about:
                intent = new Intent(context, AboutUsActivity.class);
                context.startActivity(intent);
                break;
        }
    }
}
