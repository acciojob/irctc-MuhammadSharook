package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());

        List<Station> stationList = trainEntryDto.getStationRoute();
        String route = "";
        for(int i = 0; i < stationList.size(); i++)
        {
            if(i == stationList.size()-1)
            {
                route += stationList.get(i);
            }
            else
            {
                route += stationList.get(i) + ",";
            }
        }

        train.setRoute(route);
        return trainRepository.save(train).getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket> ticketList = train.getBookedTickets();
        String []train_root = train.getRoute().split(",");

        HashMap<String,Integer> hm = new HashMap<>();

        for(int i = 0; i < train_root.length; i++)
        {
            hm.put(train_root[i],i);
        }

        if(!hm.containsKey(seatAvailabilityEntryDto.getFromStation().toString()) || !hm.containsKey(seatAvailabilityEntryDto.getToStation().toString()))
        {
            return 0;
        }

        int booked_ticket = 0;
        for(Ticket ticket : ticketList)
        {
            booked_ticket += ticket.getPassengersList().size();
        }

        int seats_available = train.getNoOfSeats() - booked_ticket;

        for(Ticket ticket : ticketList)
        {
            String fromStation = ticket.getFromStation().toString();
            String toStation = ticket.getToStation().toString();
            if(hm.get(seatAvailabilityEntryDto.getToStation().toString()) <= hm.get(fromStation))
            {
                seats_available++;
            } else if (hm.get(seatAvailabilityEntryDto.getFromStation().toString()) >= hm.get(fromStation)) {
                seats_available++;
            }
        }

       return seats_available;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.


        Train train = trainRepository.findById(trainId).get();
        String []train_route = train.getRoute().split(",");
        String req_Station = station.toString();

        boolean present = false;
        for(String s : train_route)
        {
            if(s.equals(req_Station))
            {
                present = true;
                break;
            }
        }

        if(!present)
        {
            throw new Exception("Train is not passing from this station");
        }

        int no_of_Passengers = 0;

        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket ticket : ticketList)
        {
            if(ticket.getFromStation().toString().equals(req_Station))
            {
                no_of_Passengers += ticket.getPassengersList().size();
            }
        }
        return no_of_Passengers;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();
        int max_age = Integer.MIN_VALUE;


        if(train.getBookedTickets().isEmpty())return 0;

        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket ticket : ticketList)
        {
            List<Passenger> passengerList = ticket.getPassengersList();
            for(Passenger passenger : passengerList)
            {
                max_age = Math.max(max_age,passenger.getAge());
            }
        }

        return max_age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> list = new ArrayList<>();

        List<Train> trainList = trainRepository.findAll();

        for(Train train : trainList)
        {
            String []train_route = train.getRoute().split(",");
            for(int i = 0; i < train_route.length; i++)
            {
                if(train_route[i].equals(station.toString()))
                {
                    int StartTimemin = (startTime.getHour() * 60) + startTime.getMinute();
                    int EndTimemin = (endTime.getHour() * 60) + endTime.getMinute();

                    int departureTimemin = (train.getDepartureTime().getHour() * 60) + train.getDepartureTime().getMinute();
                    int arrivalTimemin = departureTimemin + (i * 60);

                    if(arrivalTimemin >= StartTimemin && arrivalTimemin <= EndTimemin)
                    {
                        list.add(train.getTrainId());
                    }
                }
            }
        }
        return list;
    }

}
