package com.roa.foodonetv3.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.PublicationActivity;
import com.roa.foodonetv3.activities.SignInActivity;
import com.roa.foodonetv3.adapters.ReportsRecyclerAdapter;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.OnFabChangeListener;
import com.roa.foodonetv3.commonMethods.OnReceiveResponse;
import com.roa.foodonetv3.commonMethods.OnReplaceFragListener;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.GroupsDBHandler;
import com.roa.foodonetv3.db.RegisteredUsersDBHandler;
import com.roa.foodonetv3.dialogs.ReportDialog;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.model.RegisteredUser;
import com.roa.foodonetv3.model.PublicationReport;
import com.roa.foodonetv3.serverMethods.ServerMethods;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;

public class PublicationDetailFragment extends Fragment implements View.OnClickListener, ReportDialog.OnReportCreateListener{
    private static final String TAG = "PublicationDetailFrag";

    private TextView textCategory,textTimeRemaining,textJoined,textTitlePublication,textPublicationAddress,textPublicationRating,
            textPublisherName,textPublicationPrice,textPublicationDetails;
    private ImageView imagePicturePublication;
    private CircleImageView imagePublisherUser;
    private View layoutAdminDetails, layoutRegisteredDetails;
    private Publication publication;
    private ReportsRecyclerAdapter adapter;
    private FoodonetReceiver receiver;
    private int countRegisteredUsers;
    private long userID;
    private boolean isAdmin,isRegistered;
    private ArrayList<PublicationReport> reports;
    private AlertDialog alertDialog;
    private ReportDialog reportDialog;
    private RegisteredUsersDBHandler registeredUsersDBHandler;
    private OnFabChangeListener onFabChangeListener;
    private OnReceiveResponse onReceiveResponseListener;
    private OnDeletePublicationListener onDeletePublicationListener;
    private OnReplaceFragListener onReplaceFragListener;

    public PublicationDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onFabChangeListener = (OnFabChangeListener) context;
        onReceiveResponseListener = (OnReceiveResponse) context;
        onDeletePublicationListener = (OnDeletePublicationListener) context;
        onReplaceFragListener = (OnReplaceFragListener) context;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the user's ID */
        userID = CommonMethods.getMyUserID(getContext());

        // get the publication from the intent */
        publication = getArguments().getParcelable(Publication.PUBLICATION_KEY);

        // check if the user is the admin of the publication */
        isAdmin = publication != null && publication.getPublisherID() == userID;
        registeredUsersDBHandler = new RegisteredUsersDBHandler(getContext());
        if(!isAdmin){
            // the user is not the admin, check if he's a registered user for the publication */
            isRegistered = registeredUsersDBHandler.isUserRegistered(publication.getId());
        }
        setHasOptionsMenu(true);

        receiver = new FoodonetReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_publication_detail, container, false);

        // set title */
        getActivity().setTitle(publication.getTitle());

        // set recycler view */
        RecyclerView recyclerPublicationReport = (RecyclerView) v.findViewById(R.id.recyclerPublicationReport);
        recyclerPublicationReport.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportsRecyclerAdapter(getContext());
        recyclerPublicationReport.setAdapter(adapter);

        // set views */
        layoutAdminDetails = v.findViewById(R.id.layoutAdminDetails);
        layoutRegisteredDetails = v.findViewById(R.id.layoutRegisteredDetails);
        textCategory = (TextView) v.findViewById(R.id.textCategory);
        textTimeRemaining = (TextView) v.findViewById(R.id.textTimeRemaining);
        textJoined = (TextView) v.findViewById(R.id.textJoined);
        textTitlePublication = (TextView) v.findViewById(R.id.textTitlePublication);
        textPublicationAddress = (TextView) v.findViewById(R.id.textPublicationAddress);
        textPublicationRating = (TextView) v.findViewById(R.id.textPublicationRating);
        textPublisherName = (TextView) v.findViewById(R.id.textPublisherName);
        textPublicationPrice = (TextView) v.findViewById(R.id.textPublicationPrice);
        textPublicationDetails = (TextView) v.findViewById(R.id.textPublicationDetails);
        imagePicturePublication = (ImageView) v.findViewById(R.id.imagePicturePublication);
        imagePicturePublication.setOnClickListener(this);
        imagePublisherUser = (CircleImageView) v.findViewById(R.id.imagePublisherUser);
        v.findViewById(R.id.imageActionPublicationReport).setOnClickListener(this);
        v.findViewById(R.id.imageActionPublicationSMS).setOnClickListener(this);
        v.findViewById(R.id.imageActionPublicationPhone).setOnClickListener(this);
        v.findViewById(R.id.imageActionPublicationMap).setOnClickListener(this);
        v.findViewById(R.id.imageActionAdminShareFacebook).setOnClickListener(this);
        v.findViewById(R.id.imageActionAdminShareTwitter).setOnClickListener(this);
        v.findViewById(R.id.imageActionAdminSMS).setOnClickListener(this);
        v.findViewById(R.id.imageActionAdminPhone).setOnClickListener(this);

        return v;
    }

    @Override
    public void onResume() {
        // TODO: 21/11/2016 load from server every resume?
        super.onResume();
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        // TODO: 21/12/2016 should be from db
        ServerMethods.getReports(getContext(),publication.getId(),publication.getVersion());

        // initialize the views */
        initViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(reportDialog!=null && reportDialog.isShowing()){
            reportDialog.dismiss();
        }
        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(isAdmin){
            inflater.inflate(R.menu.detail_options_admin,menu);
        } else {
            inflater.inflate(R.menu.detail_options_registered,menu);
        }
    }

    /** menu for a publication */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.detail_edit:
                onReplaceFragListener.onReplaceFrags(PublicationActivity.EDIT_PUBLICATION_TAG,publication.getId());
                return true;
            case R.id.detail_take_offline:
                // TODO: 19/12/2016 add logic
                Toast.makeText(getContext(), "take offline", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.detail_delete:
                AlertDialog.Builder alertDialogDeletePublication = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.dialog_are_you_sure)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ServerMethods.deletePublication(getContext(),publication.getId());
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                alertDialog = alertDialogDeletePublication.show();
                return true;
            case R.id.detail_unregister:
                if(isRegistered){
                    AlertDialog.Builder alertDialogUnregisterPublication = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.dialog_are_you_sure)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ServerMethods.unregisterFromPublication(getContext(),publication.getId(),publication.getVersion());
                                }
                            })
                            .setNegativeButton(R.string.no, null);
                    alertDialog = alertDialogUnregisterPublication.show();
                }
                return true;
        }
        return false;
    }

    /** set the views */
    private void initViews(){
        // if the user is the admin, registered user, or a non registered user, show different layouts */
        if (isAdmin) {
            layoutAdminDetails.setVisibility(View.VISIBLE);
            layoutRegisteredDetails.setVisibility(View.GONE);

        } else if(isRegistered) {
            layoutAdminDetails.setVisibility(View.GONE);
            layoutRegisteredDetails.setVisibility(View.VISIBLE);

        } else{
            layoutAdminDetails.setVisibility(View.GONE);
            layoutRegisteredDetails.setVisibility(View.GONE);
        }
        onFabChangeListener.onFabChange(PublicationActivity.PUBLICATION_DETAIL_TAG,!isAdmin && !isRegistered);

        if(publication.getAudience()==0){
            // audience is public */
            textCategory.setText(getResources().getString(R.string.audience_public));
        } else{
            // audience is a specific group */
            GroupsDBHandler groupsDBHandler = new GroupsDBHandler(getContext());
            String groupName = groupsDBHandler.getGroupName(publication.getAudience());
            // TODO: 13/11/2016 get group name through the server 
            textCategory.setText(groupName);
            Drawable group = getResources().getDrawable(R.drawable.group);
            // TODO: 13/11/2016 check if this code works 
            textCategory.setCompoundDrawables(group,null,null,null);
        }

        String timeRemaining = String.format(Locale.US, "%1$s",
                CommonMethods.getTimeDifference(getContext(),CommonMethods.getCurrentTimeSeconds(),Double.parseDouble(publication.getEndingDate())));

        textTimeRemaining.setText(timeRemaining);
        // get the number of users registered for this publication */
        countRegisteredUsers = registeredUsersDBHandler.getPublicationRegisteredUsersCount(publication.getId());
        textJoined.setText(String.format(Locale.US,"%1$s : %2$d",getResources().getString(R.string.joined),countRegisteredUsers));
        textTitlePublication.setText(publication.getTitle());
        textPublicationAddress.setText(publication.getAddress());
        textPublisherName.setText(publication.getIdentityProviderUserName());


        String priceS;
        if(publication.getPrice()==0){
            priceS = getResources().getString(R.string.free);
        } else{
            priceS = String.valueOf(publication.getPrice());
        }
        textPublicationPrice.setText(priceS);
        textPublicationDetails.setText(publication.getSubtitle());
        File mCurrentPhotoFile = new File(CommonMethods.getPhotoPathByID(getContext(),publication.getId(),publication.getVersion()));
        if(mCurrentPhotoFile.isFile()){
            // there's an image path, try to load from file */
            Glide.with(this).load(mCurrentPhotoFile).centerCrop().into(imagePicturePublication);
        } else{
            // load default image */
            Glide.with(this).load(R.drawable.foodonet_image).centerCrop().into(imagePicturePublication);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null || userID==-1){
            // if the user is not signed in, all buttons are disabled, take him to the sign in activity */
            intent = new Intent(getContext(), SignInActivity.class);
            startActivity(intent);
        } else{
            switch (v.getId()){
                // join publication */
                case R.id.imageActionPublicationReport:
                    // if reports is null, we haven't received the reports yet and therefor, can't add a new report yet, since the user might have already sent one */
                    if(reports== null){
                        Toast.makeText(getContext(), getResources().getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
                    }
                    // if the reports were received, check if the user has previously added a report, only allow to send a new one if the user hasn't before */
                    else{
                        boolean found = false;
                        PublicationReport report;
                        for (int i = 0; i < reports.size(); i++) {
                            report = reports.get(i);
                            if(userID == report.getReportUserID()){
                                found = true;
                                break;
                            }
                        }
                        // the user has reported previously */
                        if(found){
                            Toast.makeText(getContext(), getResources().getString(R.string.you_can_only_report_once), Toast.LENGTH_SHORT).show();
                        }
                        // the user can add a new report */
                        else{
                            reportDialog = new ReportDialog(getContext(),this,publication.getTitle());
                            reportDialog.show();
                        }
                    }
                    break;

                // send SMS with message body*/
                case R.id.imageActionPublicationSMS:
                    String message = String.format("%1$s%2$s%3$s%4$s",
                            getResources().getString(R.string.sms_to_publisher_part1),
                            publication.getTitle(),
                            getResources().getString(R.string.sms_to_publisher_part2),
                            CommonMethods.getMyUserName(getContext()));
                    Uri uri = Uri.parse(String.format("smsto:%1$s",publication.getContactInfo()));
                    intent = new Intent(Intent.ACTION_SENDTO,uri);
                    intent.putExtra("sms_body",message);
                    startActivity(intent);
                    break;

                // simple intent to put the phone number in the phone's default dialer */
                case R.id.imageActionPublicationPhone:
                    if (publication.getContactInfo().matches("[0-9]+") && publication.getContactInfo().length() > 2) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + publication.getContactInfo()));
                        startActivity(intent);
                    }
                    break;

                // intent to open navigation apps */
                case R.id.imageActionPublicationMap:
                    // TODO: 22/11/2016 fix to allow both waze and google maps to work
                    if(publication.getLat()!=0 && publication.getLng()!= 0){
                        intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("geo:" + publication.getLat() + "," +
                                                publication.getLng()
//                                    + "?q=" + getStreet() + "+" +
//                                    getHousenumber() + "+" + getPostalcode() + "+" +
//                                    getCity()
                                ));
                        startActivity(intent);
                    }
                    break;

                case R.id.imageActionAdminShareFacebook:
                    // TODO: 13/02/2017 add facebook share
                    break;

                case R.id.imageActionAdminShareTwitter:
                    // TODO: 13/02/2017 add twitter share
                    break;

                case R.id.imageActionAdminSMS:
                    // TODO: 13/02/2017 check what this button needs to do and implement
                    if(countRegisteredUsers != 0){
                        RegisteredUsersDBHandler smsRegisteredUsersHandler = new RegisteredUsersDBHandler(getContext());
                        final ArrayList<RegisteredUser> smsRegisteredUsers = smsRegisteredUsersHandler.getPublicationRegisteredUsers(publication.getId());
                        String[] registeredUsersNames = new String[smsRegisteredUsers.size()];
                        for(int i = 0; i < smsRegisteredUsers.size(); i++){
                            registeredUsersNames[i] = smsRegisteredUsers.get(i).getCollectorName();
                        }
                        final boolean[] checkedItems = new boolean[smsRegisteredUsers.size()];
                        final ArrayList<Integer> selectedItemsIndexList = new ArrayList<>();

                        AlertDialog.Builder smsDialog = new AlertDialog.Builder(getContext())
                                .setTitle(R.string.dialog_select_contact)
                                .setMultiChoiceItems(registeredUsersNames, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if(isChecked){
                                            selectedItemsIndexList.add(which);
                                        } else if(selectedItemsIndexList.contains(which)){
                                            selectedItemsIndexList.remove(Integer.valueOf(which));
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        StringBuilder phoneBuilder = new StringBuilder();
                                        int itemIndex;
                                        String phone;
                                        for(int i = 0; i < selectedItemsIndexList.size(); i++){
                                            itemIndex = selectedItemsIndexList.get(i);
                                            phone = smsRegisteredUsers.get(itemIndex).getCollectorContactInfo();
                                            if (i > 0) {
                                                phoneBuilder.append(";");
                                            }
                                            phoneBuilder.append(phone);
                                        }

                                        String message = String.format("%1$s%2$s%3$s%4$s",
                                                getResources().getString(R.string.sms_to_registered_user_part1),
                                                publication.getTitle(),
                                                getResources().getString(R.string.sms_to_registered_user_part2),
                                                CommonMethods.getMyUserName(getContext()));
                                        Uri uri = Uri.parse(String.format("smsto:%1$s",phoneBuilder.toString()));
                                        final Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
                                        intent.putExtra("sms_body",message);
                                        startActivity(intent);
                                    }
                                });
                        alertDialog = smsDialog.show();
                    } else{
                        Snackbar.make(imagePicturePublication,R.string.toast_there_are_no_registered_users,Snackbar.LENGTH_LONG).show();
                    }
                    break;

                case R.id.imageActionAdminPhone:
                    if(countRegisteredUsers != 0){
                        RegisteredUsersDBHandler callRegisteredUsersHandler = new RegisteredUsersDBHandler(getContext());
                        final ArrayList<RegisteredUser> callRegisteredUsers = callRegisteredUsersHandler.getPublicationRegisteredUsers(publication.getId());
                        String[] registeredUsersNames = new String[callRegisteredUsers.size()];
                        for(int i = 0; i < callRegisteredUsers.size(); i++){
                            registeredUsersNames[i] = callRegisteredUsers.get(i).getCollectorName();
                        }
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_singlechoice, registeredUsersNames);

                        AlertDialog.Builder callDialog = new AlertDialog.Builder(getContext())
                                .setTitle(R.string.dialog_select_contact)
                                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String phone = callRegisteredUsers.get(which).getCollectorContactInfo();
                                        if (phone.matches("[0-9]+") && publication.getContactInfo().length() > 2) {
                                            final Intent callIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phone));
                                            startActivity(callIntent);
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                ;
                        alertDialog = callDialog.show();

                    } else{
                        Snackbar.make(imagePicturePublication, R.string.toast_there_are_no_registered_users,Snackbar.LENGTH_LONG).show();
                    }
                    break;

                // pressing on the publication image - open full screen view of the image */
                case R.id.imagePicturePublication:
                    // TODO: 16/01/2017 add image view logic
                    break;
            }
        }
    }

    @Override
    public void onReportCreate(int rating, short typeOfReport) {
        // send the report
        long currentTime = (long) CommonMethods.getCurrentTimeSeconds();
        PublicationReport publicationReport = new PublicationReport(-1,publication.getId(),publication.getVersion(), typeOfReport,
                CommonMethods.getDeviceUUID(getContext()),String.valueOf(currentTime),CommonMethods.getMyUserName(getContext()),
                CommonMethods.getMyUserPhone(getContext()),CommonMethods.getMyUserID(getContext()),rating);
        ServerMethods.addReport(getContext(),publicationReport);
    }

    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // receiver for reports got from the service
            int action = intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1);
            switch (action){
                // click on fab, register to publication
                case ReceiverConstants.ACTION_FAB_CLICK:
                    if(intent.getIntExtra(ReceiverConstants.FAB_TYPE,-1) == ReceiverConstants.FAB_TYPE_REGISTER_TO_PUBLICATION){
                        Intent i;
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if(user == null || userID==-1) {
                            // if the user is not signed in take him to the sign in activity */
                            i = new Intent(getContext(), SignInActivity.class);
                            startActivity(i);
                        } else{
                            RegisteredUser registeredUser = new RegisteredUser(publication.getId(),CommonMethods.getCurrentTimeSeconds(),
                                    CommonMethods.getDeviceUUID(getContext()),publication.getVersion(),CommonMethods.getMyUserName(getContext()),
                                    CommonMethods.getMyUserPhone(getContext()), CommonMethods.getMyUserID(getContext()));
                            ServerMethods.registerToPublication(getContext(),registeredUser);
                        }
                    }

                    break;

                // got reports
                case ReceiverConstants.ACTION_GET_REPORTS:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 27/11/2016 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        reports = intent.getParcelableArrayListExtra(PublicationReport.REPORT_KEY);
                        float rating = PublicationReport.getRatingFromReports(reports);
                        if(rating==-1){
                            textPublicationRating.setText(R.string.not_rated);
                        } else{
                            textPublicationRating.setText(CommonMethods.getRoundedStringFromNumber(rating));
                        }
                        adapter.updateReports(reports);
                    }
                    break;

                // registered to a publication
                case ReceiverConstants.ACTION_REGISTER_TO_PUBLICATION:
                    onReceiveResponseListener.onReceiveResponse();
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 27/11/2016 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        // registered successfully */
                        Snackbar.make(imagePicturePublication,getResources().getString(R.string.successfully_registered),Snackbar.LENGTH_LONG).show();
                        isRegistered = true;
                        initViews();
                    }
                    break;

                // unregistered from a publication
                case ReceiverConstants.ACTION_UNREGISTER_FROM_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 28/01/2017 add logic
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    }else{
                        Snackbar.make(imagePicturePublication,getResources().getString(R.string.unregistered),Snackbar.LENGTH_LONG).show();
                        isRegistered = false;
                        initViews();
                    }
                    break;

                // report added
                case ReceiverConstants.ACTION_ADD_REPORT:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 27/11/2016 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        // report registered successfully */
                        Snackbar.make(imagePicturePublication,getResources().getString(R.string.report_added),Snackbar.LENGTH_LONG).show();
                    }
                    break;

                // publication deleted
                case ReceiverConstants.ACTION_DELETE_PUBLICATION:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 19/12/2016 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getLongExtra(Publication.PUBLICATION_ID,-1)==publication.getId()){
                            Toast.makeText(context, getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                            onDeletePublicationListener.onDeletePublication();
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_GOT_NEW_REPORT:
                    if(alertDialog!=null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 02/04/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        if(publication.getId() == intent.getLongExtra(Publication.PUBLICATION_ID,-1) && intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            ServerMethods.getReports(getContext(),publication.getId(),publication.getVersion());
                        }
                    }
                    break;
            }
        }
    }

    public interface OnDeletePublicationListener {
        void onDeletePublication();
    }
}
