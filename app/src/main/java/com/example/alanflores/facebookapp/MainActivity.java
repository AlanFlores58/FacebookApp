package com.example.alanflores.facebookapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.Sharer.Result;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION = "piblish_actions";
    private Button postStatusUpdateButton;
    private ProfilePictureView profilePictureView;
    private TextView greeting;
    private boolean canPresentSharedDialog;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private ShareDialog shareDialog;

    private FacebookCallback<Result> sharedCallback = new FacebookCallback<Result>() {
        @Override
        public void onSuccess(Result result) {
            if(result.getPostId() != null){
                showMessage("Publicacion enviada.");
            }

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {
            showMessage("Error al crear la publicacion");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                upDateUI();
            }

            @Override
            public void onCancel() {
                showMessage("No se han dado los permisos para publicar");
                upDateUI();
            }

            @Override
            public void onError(FacebookException error) {
                showMessage("A ocurrido un error al iniciar sesion");
                upDateUI();
            }
        });

        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, sharedCallback);

        setContentView(R.layout.activity_main);
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                upDateUI();
            }
        };

        profilePictureView = (ProfilePictureView)findViewById(R.id.profilePicture);
        greeting = (TextView) findViewById(R.id.greeting);
        postStatusUpdateButton = (Button) findViewById(R.id.postStatus);
        postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickPostStatusUpdate();
            }
        });
        canPresentSharedDialog = ShareDialog.canShow(ShareLinkContent.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getApplication());
        upDateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //AppEventsLogger.deactivateApp(getApplication());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    private void upDateUI(){
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;
        postStatusUpdateButton.setEnabled(enableButtons || canPresentSharedDialog);

        Profile profile = Profile.getCurrentProfile();
        if(enableButtons && profile != null){
            profilePictureView.setProfileId(profile.getId());
            greeting.setText("Hola " + profile.getFirstName());
        } else {
            profilePictureView.setProfileId(null);
            greeting.setText("");
        }

    }

    private void onClickPostStatusUpdate(){
        postStatusUpdate();
    }

    private void postStatusUpdate(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null || canPresentSharedDialog){
            Profile profile = Profile.getCurrentProfile();
            ShareLinkContent shareLinkContent = new ShareLinkContent.Builder().build();
            if(canPresentSharedDialog)
                shareDialog.show(shareLinkContent);
            else if(profile != null && hasPublishPermission()){
                ShareApi.share(shareLinkContent,sharedCallback);
            }
        }
    }

    private boolean hasPublishPermission(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains(PERMISSION);
    }

    private void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
