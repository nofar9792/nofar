package com.example.nofarcohenzedek.dogo;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nofarcohenzedek.dogo.Model.Dog;
import com.example.nofarcohenzedek.dogo.Model.DogOwner;
import com.example.nofarcohenzedek.dogo.Model.DogSize;
import com.example.nofarcohenzedek.dogo.Model.DogWalker;
import com.example.nofarcohenzedek.dogo.Model.Model;
import com.example.nofarcohenzedek.dogo.Model.User;
import com.example.nofarcohenzedek.dogo.Model.Utilities;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MyProfileFragment extends Fragment {
    private Boolean isOwner;
    private Long id;
    private String userName;
    private EditText firstName;
    private EditText lastName;
    private EditText phone;
    private EditText address;
    private EditText city;
    private EditText age;
    private EditText price;
    private CheckBox morning;
    private CheckBox afternoon;
    private CheckBox evening;
    private EditText dogName;
    private RadioButton isBig;
    private RadioButton isMedium;
    private RadioButton isSmall;
    private EditText dogAge;
    private String dogPic;
    private String errorMessage;
    private static final int SELECT_PHOTO = 100;
    private ProgressBar progressBar;
    private Context context;
    View currentView;

    public MyProfileFragment()
    {

    }

    public MyProfileFragment(Long userId, boolean IsOwner)
    {
        id = userId;
        isOwner = IsOwner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        context = container.getContext();
        View view = inflater.inflate(R.layout.activity_my_profile, container, false);
        currentView = view;
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle args = getArguments();
        progressBar = (ProgressBar) view.findViewById(R.id.myProfileProgressBar);

       // isOwner = args.getBoolean("isOwner");

        if (!isOwner)
        {
            view.findViewById(R.id.dogWalkerSectionInMyProfile).setVisibility(View.VISIBLE);
        }
        else
        {
            view.findViewById(R.id.dogOwnerSectionInMyProfile).setVisibility(View.VISIBLE);
        }

        Model.getInstance().getUserById(id, new Model.GetUserListener() {
              @Override
            public void onResult(User user) {
                id = user.getId();
                userName = user.getUserName();

                firstName = (EditText) currentView.findViewById(R.id.firstNameMP);
                lastName = (EditText) currentView.findViewById(R.id.lastNameMP);
                phone = (EditText) currentView.findViewById(R.id.phoneNumberMP);
                address = (EditText) currentView.findViewById(R.id.addressMP);
                city = (EditText) currentView.findViewById(R.id.cityMP);
                age = (EditText) currentView.findViewById(R.id.ageMP);
                price = (EditText) currentView.findViewById(R.id.priceForHourMP);
                morning = (CheckBox) currentView.findViewById(R.id.cbx_isComfortableOnMorningMP);
                afternoon = (CheckBox) currentView.findViewById(R.id.cbx_isComfortableOnAfternoonMP);
                evening = (CheckBox) currentView.findViewById(R.id.cbx_isComfortableOnEveningMP);
                dogName = (EditText) currentView.findViewById(R.id.dogNameMP);
                isBig = (RadioButton) currentView.findViewById(R.id.isBigMP);
                isMedium = (RadioButton) currentView.findViewById(R.id.isMediumMP);
                isSmall = (RadioButton) currentView.findViewById(R.id.isSmallMP);
                dogAge = (EditText) currentView.findViewById(R.id.dogAgeMP);

                firstName.setText(user.getFirstName());
                lastName.setText(user.getLastName());
                phone.setText(user.getPhoneNumber());
                address.setText(user.getAddress());
                city.setText(user.getCity());

                if (!isOwner) {
                    age.setText(Long.toString(((DogWalker) user).getAge()));
                    price.setText(Long.toString(((DogWalker) user).getPriceForHour()));
                    morning.setChecked(((DogWalker) user).isComfortableOnMorning());
                    afternoon.setChecked(((DogWalker) user).isComfortableOnAfternoon());
                    evening.setChecked(((DogWalker) user).isComfortableOnEvening());
                } else {
                    dogName.setText(((DogOwner) user).getDog().getName());
                    DogSize size = ((DogOwner) user).getDog().getSize();
                    isBig.setChecked((size == DogSize.Large));
                    isMedium.setChecked((size == DogSize.Medium));
                    isSmall.setChecked((size == DogSize.Small));
                    dogAge.setText(Long.toString(((DogOwner) user).getDog().getAge()));

                    // Load the picture of dog
                    dogPic = (((DogOwner) user).getDog().getPicRef());

                    // Check if there is a picture to this dog
                    if (dogPic != null) {
                        // if possible - take from device
                        if (Utilities.isFileExistInDevice(dogPic)) {
                            ((ImageView) currentView.findViewById(R.id.dogPicMP)).setImageBitmap(Utilities.loadImageFromDevice(dogPic));
                        } else {
                            Model.getInstance().getImage(dogPic, new Model.GetBitmapListener() {
                                @Override
                                public void onResult(Bitmap picture) {
                                    ((ImageView) currentView.findViewById(R.id.dogPicMP)).setImageBitmap(picture);

                                    Utilities.saveImageOnDevice(dogPic, picture);
                                }
                            });
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.saveChangesMyProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChangesClick(v);
            }
        });

        return view;
    }

    public void saveChangesClick(View view)
    {
        if (isValid())
        {
            String firstNameVal = firstName.getText().toString();
            String lastNameVal = lastName.getText().toString();
            String phoneVal = phone.getText().toString();
            String addressVal = address.getText().toString();
            String cityVal = city.getText().toString();

            if (isOwner)
            {
                String dogNameVal = dogName.getText().toString();
                Long dogAgeVal = Long.valueOf(dogAge.getText().toString());
                DogSize sizeVal = (isSmall.isChecked() ? DogSize.Small : (isMedium.isChecked() ? DogSize.Medium : DogSize.Large));

                progressBar.setVisibility(View.VISIBLE);
                Model.getInstance().updateDogOwner(new DogOwner(id, userName, firstNameVal, lastNameVal, phoneVal, addressVal, cityVal,
                        new Dog(dogNameVal, sizeVal, dogAgeVal, dogPic)), new Model.IsSucceedListener() {
                    @Override
                    public void onResult(boolean isSucceed) {
                        if(isSucceed){
                            Toast.makeText(context , "שמירה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "אירעה שגיאה בתהליך השמירה, אנא נסה שוב", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
            else
            {
                Long ageVal = Long.valueOf(age.getText().toString());
                int priceVal = Integer.valueOf(price.getText().toString());
                Boolean morningVal = morning.isChecked();
                Boolean noonVal = afternoon.isChecked();
                Boolean eveningVal = evening.isChecked();

                progressBar.setVisibility(View.VISIBLE);
                Model.getInstance().updateDogWalker(new DogWalker(id, userName, firstNameVal, lastNameVal, phoneVal, addressVal, cityVal,
                        ageVal, priceVal, morningVal, noonVal, eveningVal), new Model.IsSucceedListener() {
                    @Override
                    public void onResult(boolean isSucceed) {
                        progressBar.setVisibility(View.GONE);
                        if(isSucceed){
                            Toast.makeText(context, "שמירה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "אירעה שגיאה בתהליך השמירה, אנא נסה שוב", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode)
        {
            case SELECT_PHOTO:
                if(resultCode == getActivity().RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);

                    dogPic = selectedImage.getPath();
                    ((ImageView)currentView.findViewById(R.id.dogPicMP)).setImageBitmap(yourSelectedImage);
                }
        }
    }

    private boolean isValid()
    {
        boolean isValid = true;
        errorMessage = "";

        if(firstName.getText().toString().isEmpty() ||
            lastName.getText().toString().isEmpty() ||
            phone.getText().toString().isEmpty() ||
                city.getText().toString().isEmpty() ||
                address.getText().toString().isEmpty())
        {
            errorMessage = "אנא מלא את כל שדות ההרשמה.";
            isValid = false;
        }
        else if (isOwner)
        {
            // todo: i remove "dogPic.isEmpty() ||" maybe picture is not must
            if (dogName.getText().toString().isEmpty() || dogAge.getText().toString().isEmpty() ||
                    (!isBig.isChecked() && !isMedium.isChecked() && !isSmall.isChecked()))
            {
                errorMessage = "אנא מלא את פרטי הכלב.";
                isValid = false;
            }
        }
        else
        {
            if (price.getText().toString().isEmpty() || age.getText().toString().isEmpty())
            {
                errorMessage = "אנא מלא את כל הפרטים.";
                isValid = false;
            }
        }

        TextView error = (TextView)currentView.findViewById(R.id.error);
        error.setText(errorMessage);

        return isValid;
    }
}
