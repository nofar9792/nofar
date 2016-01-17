package com.example.nofarcohenzedek.dogo.Model.Parse;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.nofarcohenzedek.dogo.Model.Dog;
import com.example.nofarcohenzedek.dogo.Model.DogOwner;
import com.example.nofarcohenzedek.dogo.Model.DogWalker;
import com.example.nofarcohenzedek.dogo.Model.Model;
import com.example.nofarcohenzedek.dogo.Model.Request;
import com.example.nofarcohenzedek.dogo.Model.RequestStatus;
import com.example.nofarcohenzedek.dogo.Model.Trip;
import com.example.nofarcohenzedek.dogo.Model.User;
import com.parse.Parse;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nofar Cohen Zedek on 02-Jan-16.
 */
public class ModelParse {
    //region Ctor
    public ModelParse(Context context) {
        Parse.initialize(context, "ClCDxIalYQPR6IrVXUHtHQW99tazxTZOAFUnanLB", "8EqrsIlUMqsfx8yxs9FjUndl0AO58cDHIJFp6wyY");
    }
    //endregion

    //region User Methods
    public void logIn(String userName, String password, final Model.GetUserListener listener) {
        UserParse.logIn(userName, password, new Model.GetUserListener() {
            @Override
            public void onResult(User user) {
                if (user != null) {
                    addDetailsByInstance(user);
                }
                listener.onResult(user);
            }
        });
    }

    public void getUserById(long userId, final Model.GetUserListener listener) {
        UserParse.getUserById(userId, new Model.GetUserListener() {
            @Override
            public void onResult(final User user) {
                addDetailsByInstance(user);
                listener.onResult(user);
            }
        });
    }

    private void addDetailsByInstance(final User user) {
        if (user instanceof DogWalker) {
            DogWalkerParse.addDogWalkerDetails((DogWalker) user);
        } else {
            ((DogOwner) user).setDog(DogParse.getDogByUserIdSync(user.getId()));
        }
    }

    public void logOut() {
        UserParse.logOut();
    }

    //endregion

    public void getDogByUserId(long userId, final Model.GetDogListener listener) {
        DogParse.getDogByUserId(userId, listener);
    }

    //endregion

    //region Dog Walker Methods

    public DogWalker getDogWalkerByIdSync(long userId) {
        User user = UserParse.getUserByIdSync(userId);
        if(user != null){
            DogWalkerParse.addDogWalkerDetails((DogWalker) user);
            return (DogWalker)user;
        }
        return null;
    }

    public void getAllDogWalkers(String fromDate, final Model.GetDogWalkersListener listener) {
        UserParse.getDogWalkerUsers(fromDate, new Model.GetDogWalkersListener() {
            @Override
            public void onResult(List<DogWalker> dogWalkers) {
                for (final DogWalker dogWalker : dogWalkers) {
                    DogWalkerParse.addDogWalkerDetails(dogWalker);
                }
                listener.onResult(dogWalkers);
            }
        });
    }

    public long addDogWalker(String userName, String password, String firstName, String lastName, String phoneNumber,
                             String address, String city, long age, int priceForHour, boolean isComfortableOnMorning, boolean isComfortableOnAfternoon, boolean isComfortableOnEvening) throws Exception {
        long newUserId = UserParse.addToUsersTable(userName, password, firstName, lastName, phoneNumber, address, city, true);
        DogWalkerParse.addToDogWalkersTable(newUserId, age, priceForHour, isComfortableOnMorning, isComfortableOnAfternoon, isComfortableOnEvening);
        return newUserId;
    }

    public void updateDogWalker(DogWalker dogWalker){
        UserParse.updateUser(dogWalker);
        DogWalkerParse.updateDogWalker(dogWalker);
    }
    //endregion

    //region Dog Owner Methods

    public DogOwner getDogOwnerByIdSync(long userId) {
        final User user = UserParse.getUserByIdSync(userId);
        if (user != null) {
            ((DogOwner) user).setDog(DogParse.getDogByUserIdSync(userId));
            return (DogOwner) user;
        }
        return null;
    }

    public long addDogOwner(String userName, String password, String firstName, String lastName, String phoneNumber,
                            String address, String city, Dog dog) throws Exception {
        long newUserId = UserParse.addToUsersTable(userName, password, firstName, lastName, phoneNumber, address, city, false);
        DogParse.addToDogsTable(newUserId, dog);

        return newUserId;
    }

    public void updateDogOwner(DogOwner dogOwner){
        UserParse.updateUser(dogOwner);
        DogParse.updateDog(dogOwner.getId(), dogOwner.getDog());
    }

    //endregion

    //endregion

    //region Trip Methods
    public void getTripsByDogOwnerId(long dogOwnerId, final Model.GetTripsListener listener){
        TripParse.getTripsDetailsByDogOwnerId(dogOwnerId, new GetTripsDetailsListener() {
            @Override
            public void onResult(final List<Trip> trips) {
                for (final Trip trip : trips) {
                    trip.setDogWalker(getDogWalkerByIdSync(trip.getDogWalkerId()));
                    trip.setDogOwner(getDogOwnerByIdSync(trip.getDogOwnerId()));
                }
                listener.onResult(trips);
            }
        });
    }

    public void getTripsByDogWalkerId(long dogWalkerId, final Model.GetTripsListener listener) {
        TripParse.getTripsDetailsByDogWalkerId(dogWalkerId, new GetTripsDetailsListener() {
            @Override
            public void onResult(final List<Trip> trips) {
                for (final Trip trip : trips) {
                    trip.setDogOwner(getDogOwnerByIdSync(trip.getDogOwnerId()));
                    trip.setDogWalker(getDogWalkerByIdSync(trip.getDogWalkerId()));
                }
                listener.onResult(trips);
            }
        });
    }

    public long startTrip(long dogOwnerId, long dogWalkerId) {
        return TripParse.startTrip(dogOwnerId, dogWalkerId);
    }

    public void endTrip(long tripId) {
        TripParse.endTrip(tripId);
    }

    public void payTrip(long tripId) {
        TripParse.changeTripToPaid(tripId);
    }

    // endregion

    //region Request Methods
    public void addRequest(long dogOwnerId, long dogWalkerId) {
        RequestParse.addToRequestTable(dogOwnerId, dogWalkerId, RequestStatus.Waiting);
    }

    public void acceptRequest(long dogOwnerId, long dogWalkerId) {
        RequestParse.updateRequest(dogOwnerId, dogWalkerId, RequestStatus.Accepted);
    }

    public void declineRequest(long dogOwnerId, long dogWalkerId) {
        RequestParse.updateRequest(dogOwnerId, dogWalkerId, RequestStatus.Declined);
    }

    // todo: im not sure we need this func
    public void getWalkersConnectToOwner(long dogOwnerId, final Model.GetDogWalkersListener listener) {
        RequestParse.getWalkersIdsConnectedToOwner(dogOwnerId, new GetIdsListener() {
            @Override
            public void onResult(List<Long> ids) {
                final List<DogWalker> dogWalkers = new LinkedList<>();
                for (long dogWalkerId : ids) {
                    dogWalkers.add(getDogWalkerByIdSync(dogWalkerId));
                }
                listener.onResult(dogWalkers);
            }
        });
    }

    public void getOwnersConnectToWalker(long dogWalkerId, final Model.GetDogOwnersListener listener) {
        RequestParse.getOwnersIdsConnectedToWalker(dogWalkerId, new GetIdsListener() {
            @Override
            public void onResult(List<Long> ids) {
                final List<DogOwner> dogOwners = new LinkedList<>();
                for (long dogWalkerId : ids) {
                    dogOwners.add(getDogOwnerByIdSync(dogWalkerId));
                }
                listener.onResult(dogOwners);
            }
        });
    }

    /*
     * get waiting requests for dog walker
     */
    public void getRequestForDogWalker(long dogWalkerId, final Model.GetDogOwnersListener listener){
        RequestParse.getRequestForDogWalker(dogWalkerId, new GetIdsListener() {
            @Override
            public void onResult(List<Long> ids) {
                final List<DogOwner> dogOwners = new LinkedList<>();
                for (long dogWalkerId : ids) {
                    dogOwners.add(getDogOwnerByIdSync(dogWalkerId));
                }
                listener.onResult(dogOwners);
            }
        });
    }

    /*
     * get waiting requests of dog owner
     */
    public void getRequestOfDogOwner(long dogOwnerId, final Model.GetDogWalkersListener listener){
        RequestParse.getRequestOfDogOwner(dogOwnerId, new GetIdsListener() {
            @Override
            public void onResult(List<Long> ids) {
                final List<DogWalker> dogWalkers = new LinkedList<>();
                for (long dogWalkerId : ids) {
                    dogWalkers.add(getDogWalkerByIdSync(dogWalkerId));
                }
                listener.onResult(dogWalkers);
            }
        });
    }

    /*
     * get all status requests of dog walker
     */
    public void getRequestByDogWalker(long dogWalkerId, String fromDate, final Model.GetRequestsListener listener){
        RequestParse.getRequestByDogWalker(dogWalkerId, fromDate, new Model.GetRequestsListener() {
            @Override
            public void onResult(List<Request> requests) {
                for (Request request : requests) {
                    request.setDogOwner(getDogOwnerByIdSync(request.getDogOwnerId()));
                }
                listener.onResult(requests);
            }
        });
    }

    /*
     * get all status requests of dog owner
     */
    public void getRequestByDogOwner(long dogOwnerId, String fromDate, final Model.GetRequestsListener listener) {
        RequestParse.getRequestByDogOwner(dogOwnerId, fromDate, new Model.GetRequestsListener() {
            @Override
            public void onResult(List<Request> requests) {
                for (Request request : requests) {
                    request.setDogWalker(getDogWalkerByIdSync(request.getDogWalkerId()));
                }
                listener.onResult(requests);
            }
        });
    }

    //endregion

    //region Image Methods

    public void saveImage(String imageName, Bitmap picture){
        ImageParse.addToImagesTable(imageName,picture);
    }

    public void getImage(String imageName, Model.GetBitmapListener listener){
        ImageParse.getImage(imageName, listener);
    }

    //endregion

    //region Interfaces
    public interface GetIdsListener {
        void onResult(List<Long> ids);
    }

    public interface GetTripsDetailsListener {
        void onResult(List<Trip> trips);
    }
    //endregion
}
