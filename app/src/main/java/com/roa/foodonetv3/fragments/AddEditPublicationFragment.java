package com.roa.foodonetv3.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.LatestPlacesActivity;
import com.roa.foodonetv3.activities.PublicationActivity;
import com.roa.foodonetv3.activities.SplashForCamera;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.DecimalDigitsInputFilter;
import com.roa.foodonetv3.commonMethods.OnGotMyUserImageListener;
import com.roa.foodonetv3.commonMethods.OnReceiveResponseListener;
import com.roa.foodonetv3.commonMethods.OnReplaceFragListener;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.GroupsDBHandler;
import com.roa.foodonetv3.model.Group;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.model.SavedPlace;
import com.roa.foodonetv3.serverMethods.ServerMethods;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AddEditPublicationFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = "AddEditPublicationFrag";
    private static final int INTENT_TAKE_PICTURE = 1;
    private static final int INTENT_PICK_PICTURE = 2;
    private EditText editTextTitleAddPublication, editTextPriceAddPublication, editTextDetailsAddPublication;
    private Spinner spinnerShareWith;
    private TextView textLocationAddPublication, textPublicationPriceType;
    private double endingDate;
    private String mCurrentPhotoPath, pickPhotoPath;
    private ImageView imagePictureAddPublication;
    private SavedPlace place;
    private Publication publication;
//    private boolean isEdit;
    private String editType;
    private ArrayList<Group> groups;
    private ArrayAdapter<String> spinnerAdapter;
    private FoodonetReceiver receiver;
    private View layoutInfo;
    private OnReceiveResponseListener onReceiveResponseListener;
    private OnReplaceFragListener onReplaceFragListener;


    public AddEditPublicationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onReceiveResponseListener = (OnReceiveResponseListener) context;
        onReplaceFragListener = (OnReplaceFragListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // local image path that will be used for saving locally and uploading the file name to the server*/
        mCurrentPhotoPath = "";

        receiver = new FoodonetReceiver();
        editType = getArguments().getString(TAG,PublicationActivity.ADD_PUBLICATION_TAG);

        if(editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)){
            // if there's a publication in the intent - it is an edit of an existing publication */
            if (savedInstanceState == null) {
                // also check if there's a savedInstanceState, if there isn't - load the publication, if there is - load from savedInstanceState */
                publication = getArguments().getParcelable(Publication.PUBLICATION_KEY);
                place = new SavedPlace(publication.getAddress(),publication.getLat(),publication.getLng());
            } else {
                // TODO: 19/11/2016 add savedInstanceState reader
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_edit_publication, container, false);

        String title = getString(R.string.new_share);

        // set layouts */
        editTextTitleAddPublication = (EditText) v.findViewById(R.id.editTextTitleAddPublication);
        textLocationAddPublication = (TextView) v.findViewById(R.id.textLocationAddPublication);
        textLocationAddPublication.setOnClickListener(this);
        spinnerShareWith = (Spinner) v.findViewById(R.id.spinnerShareWith);
        spinnerAdapter = new ArrayAdapter<>(getContext(),R.layout.item_spinner_groups,R.id.textGroupName);
        spinnerShareWith.setAdapter(spinnerAdapter);
        editTextDetailsAddPublication = (EditText) v.findViewById(R.id.editTextDetailsAddPublication);
        editTextPriceAddPublication = (EditText) v.findViewById(R.id.editTextPriceAddPublication);
        editTextPriceAddPublication.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        v.findViewById(R.id.imageTakePictureAddPublication).setOnClickListener(this);
        imagePictureAddPublication = (ImageView) v.findViewById(R.id.imagePictureAddPublication);

        // currently only supporting NIS
        textPublicationPriceType = (TextView) v.findViewById(R.id.textPublicationPriceType);
        textPublicationPriceType.setText(getString(R.string.currency_nis));

        if(editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)){
            title = publication.getTitle();
            spinnerShareWith.setEnabled(false);
            mCurrentPhotoPath = CommonMethods.getFilePathFromPublicationID(getContext(),publication.getId(),publication.getVersion());
            File mCurrentPhotoFile = null;
            if (mCurrentPhotoPath != null) {
                mCurrentPhotoFile = new File(mCurrentPhotoPath);
                if(mCurrentPhotoFile.isFile()){
                    // there's an image path, try to load from file */
                    Glide.with(this).load(mCurrentPhotoFile).centerCrop().into(imagePictureAddPublication);
            }
            } else{
                // load default image */
                Glide.with(this).load(R.drawable.foodonet_image).centerCrop().into(imagePictureAddPublication);
            }
        }
        getActivity().setTitle(title);

        layoutInfo = v.findViewById(R.id.layoutInfo);
        layoutInfo.setVisibility(View.GONE);
        TextView textInfo = (TextView) v.findViewById(R.id.textInfo);
        textInfo.setText(R.string.start_sharing_by_adding_an_image_of_the_food_you_wish_to_share);
        textInfo.setTextSize(getResources().getDimension(R.dimen.text_size_12));
        if (editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)){
            loadPublicationIntoViews();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        GroupsDBHandler groupsDBHandler = new GroupsDBHandler(getContext());

        spinnerAdapter.clear();
        if (editType.equals(PublicationActivity.EDIT_PUBLICATION_TAG) || editType.equals(PublicationActivity.REPUBLISH_PUBLICATION_TAG)){
            Group group = groupsDBHandler.getGroup(publication.getAudience());
            spinnerAdapter.add(group.getGroupName());
            if(groups== null){
                groups = new ArrayList<>();
            }else{
                groups.clear();
            }
            groups.add(group);
        } else{
            groups = groupsDBHandler.getAllGroupsWithPublic();
            String[] groupsNames = new String[groups.size()];
            Group group;
            String groupName;
            for (int i = 0; i < groups.size(); i++) {
                group = groups.get(i);
                groupName = group.getGroupName();
                groupsNames[i] = groupName;
            }
            spinnerAdapter.addAll(groupsNames);
        }

        if (mCurrentPhotoPath == null|| mCurrentPhotoPath.equals("")) {
            layoutInfo.setVisibility(View.VISIBLE);

            imagePictureAddPublication.setVisibility(View.GONE);
        } else{
            layoutInfo.setVisibility(View.GONE);
            imagePictureAddPublication.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    public void loadPublicationIntoViews() {
        editTextTitleAddPublication.setText(publication.getTitle());
        textLocationAddPublication.setText(publication.getAddress());
        // TODO: 29/12/2016 add logic to spinner
//        spinnerShareWith.set
        editTextDetailsAddPublication.setText(publication.getSubtitle());
        editTextPriceAddPublication.setText(String.valueOf(publication.getPrice()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageTakePictureAddPublication:
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.add_image)
                        .setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // start the popup activity*/
                                getContext().startActivity(new Intent(getContext(), SplashForCamera.class));
                                // wait for the popup activity before starting the camera */
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        synchronized (getContext()) {
                                            try {
                                                getContext().wait(CommonConstants.SPLASH_CAMERA_TIME);
                                                // starts the image taking intent through the default app*/
                                                dispatchTakePictureIntent();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                };
                                thread.start();
                            }
                        })
                        .setNegativeButton(R.string.gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // get image from gallery */
                                dispatchPickPictureIntent();
                            }
                        });
                dialog.show();

                break;
            case R.id.textLocationAddPublication:
                // start the google places select activity */
                startActivityForResult(new Intent(getContext(), LatestPlacesActivity.class), LatestPlacesActivity.REQUEST_PLACE_PICKER);
                break;
        }
    }

    /** starts the image taking intent through the default app*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = CommonMethods.createImageFile(getContext());
                pickPhotoPath = photoFile.getPath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null && photoFile.isFile()) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.roa.foodonetv3.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, INTENT_TAKE_PICTURE);
            }
        }
    }

    /** get image from gallery app installed */
    private void dispatchPickPictureIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        File photoFile = null;
        try {
            photoFile = CommonMethods.createImageFile(getContext());
            pickPhotoPath = photoFile.getPath();
            Log.d(TAG, "photo path: " + pickPhotoPath);
            startActivityForResult(chooserIntent, INTENT_PICK_PICTURE);
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {

                // an image was successfully taken, since we have the path already,
                //  we'll run the editOverwriteImage method that scales down, shapes and overwrites the images in the path
                //  returns true if successful*/
                case INTENT_TAKE_PICTURE:
                    mCurrentPhotoPath = pickPhotoPath;
                    try {
                        Glide.with(this).load(CommonMethods.compressImage(getContext(), null, mCurrentPhotoPath)).centerCrop().into(imagePictureAddPublication);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                // an image was successfully picked, since we have the path already,
                //  we'll run the editOverwriteImage method that scales down, shapes and overwrites the images in the path
                //  returns true if successful*/
                case INTENT_PICK_PICTURE:
                    mCurrentPhotoPath = pickPhotoPath;
                    try {
                        Glide.with(this).load(CommonMethods.compressImage(getContext(), data.getData(), mCurrentPhotoPath)).centerCrop().into(imagePictureAddPublication);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case LatestPlacesActivity.REQUEST_PLACE_PICKER:
                    place = data.getParcelableExtra("place");
                    textLocationAddPublication.setText(place.getAddress());
                    break;
            }
        }
    }

    public void uploadPublicationToServer() {
        // upload the publication to the foodonet server */
        String contactInfo = CommonMethods.getMyUserPhone(getContext());
        String title = editTextTitleAddPublication.getText().toString();
        String location = textLocationAddPublication.getText().toString();
        String priceS = editTextPriceAddPublication.getText().toString();
        String details = editTextDetailsAddPublication.getText().toString();
        // currently starting time is now */
        double startingDate = CommonMethods.getCurrentTimeSeconds();
        if (endingDate == 0) {
            // default ending date is 2 days after creation */
            endingDate = startingDate + CommonConstants.TIME_SECONDS_NORMAL_PUBLICATION_DURATION;
        }
        long localPublicationID = -1;
        switch (editType){
            case PublicationActivity.ADD_PUBLICATION_TAG:
                localPublicationID = CommonMethods.getNewLocalPublicationID();
                break;
            case PublicationActivity.EDIT_PUBLICATION_TAG:
                localPublicationID = publication.getId();
                break;
            case PublicationActivity.REPUBLISH_PUBLICATION_TAG:
                // keeping the original id so that it will be deleted in server methods
                localPublicationID = publication.getId();
                break;
        }
//        if (!isEdit) {

//        } else {
//            localPublicationID = publication.getId();
//        }
        if(mCurrentPhotoPath==null || mCurrentPhotoPath.equals("")){
            mCurrentPhotoPath = null;
        }
        if (title.equals("") || location.equals("") || place.getLat()== CommonConstants.LATLNG_ERROR || place.getLng()==CommonConstants.LATLNG_ERROR
                || mCurrentPhotoPath == null) {
            Toast.makeText(getContext(), R.string.post_please_enter_all_fields, Toast.LENGTH_SHORT).show();
            onReceiveResponseListener.onReceiveResponse();
        } else {
            double price;
            if (priceS.equals("")) {
                price = 0.0;
            } else {
                try {
                    price = Double.parseDouble(priceS);
                } catch (NumberFormatException e) {
                    Log.e("PublicationActivity", e.getMessage());
                    Toast.makeText(getContext(), R.string.post_toast_please_enter_a_price_in_numbers, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            publication = new Publication(localPublicationID, -1, title, details, location, (short) 2, place.getLat(), place.getLng(),
                    String.valueOf(startingDate), String.valueOf(endingDate), contactInfo, true, CommonMethods.getDeviceUUID(getContext()),
                    mCurrentPhotoPath,
                    CommonMethods.getMyUserID(getContext()),
                    groups.get(spinnerShareWith.getSelectedItemPosition()).getGroupID() , CommonMethods.getMyUserName(getContext()), price, "");
            switch (editType){
                case PublicationActivity.ADD_PUBLICATION_TAG:
                    ServerMethods.addPublication(getContext(),publication);
                    break;
                case PublicationActivity.EDIT_PUBLICATION_TAG:
                    ServerMethods.editPublication(getContext(),publication);
                    break;
                case PublicationActivity.REPUBLISH_PUBLICATION_TAG:
                    ServerMethods.republishPublication(getContext(),publication);
                    break;
            }
        }
    }


    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // receiver for reports got from the service */
            int action = intent.getIntExtra(ReceiverConstants.ACTION_TYPE, -1);
            switch (action) {
                case ReceiverConstants.ACTION_FAB_CLICK:
                    // button for uploading the publication to the server */
                    if (intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR, false)) {
                        // TODO: 18/12/2016 add logic if fails
                        Toast.makeText(context, "fab failed", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadPublicationToServer();
                    }
                    break;

                case ReceiverConstants.ACTION_ADD_PUBLICATION:
                case ReceiverConstants.ACTION_EDIT_PUBLICATION:
                    onReceiveResponseListener.onReceiveResponse();
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 27/11/2016 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        onReplaceFragListener.onReplaceFrags(PublicationActivity.NEW_STACK_TAG,-1);
                    }
                    break;

                case ReceiverConstants.ACTION_DELETE_PUBLICATION:
                    onReceiveResponseListener.onReceiveResponse();
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 21/05/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        if(publication.getId()==intent.getLongExtra(Publication.PUBLICATION_ID,-1)){
                            onReplaceFragListener.onReplaceFrags(PublicationActivity.NEW_STACK_TAG,-1);
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_SAVE_USER_IMAGE:
                    OnGotMyUserImageListener onGotMyUserImageListener = (OnGotMyUserImageListener) getContext();
                    onGotMyUserImageListener.gotMyUserImage();
                    break;
            }
        }
    }
}
