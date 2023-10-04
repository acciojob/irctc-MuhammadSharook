package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int seatsBooked = 0;

        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket ticket : ticketList)
        {
            seatsBooked += ticket.getPassengersList().size();
        }

        if(seatsBooked + bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats())
        {
            throw new Exception("Less tickets are available");
        }

        String []train_route = train.getRoute().split(",");
        int fromStation = -1;
        int toStation = -1;

        for(int i = 0; i < train_route.length; i++)
        {
            if(bookTicketEntryDto.getFromStation().toString().equals(train_route[i]))
            {
                fromStation = i;
                break;
            }
        }
        for(int i = 0; i < train_route.length; i++)
        {
            if(bookTicketEntryDto.getToStation().toString().equals(train_route[i]))
            {
                toStation = i;
                break;
            }
        }

        if(fromStation == -1 || toStation == -1 || toStation - fromStation < 0)
        {
            throw new Exception("Invalid stations");
        }

        List<Passenger> passengerList = new ArrayList<>();
        List<Integer> passengersIds = bookTicketEntryDto.getPassengerIds();

        for(int id : passengersIds)
        {
            passengerList.add(passengerRepository.findById(id).get());
        }

        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);

        int fare = bookTicketEntryDto.getNoOfSeats() * (toStation - fromStation) * 300;
        ticket.setTotalFare(fare);

        train.getBookedTickets().add(ticket);
        train.setNoOfSeats(train.getNoOfSeats() - bookTicketEntryDto.getNoOfSeats());

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);

        trainRepository.save(train);

       return ticketRepository.save(ticket).getTicketId();

    }
}
