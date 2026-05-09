package com.vaibhav.StayNexus.Service;

import com.vaibhav.StayNexus.Dto.GuestDTO;
import com.vaibhav.StayNexus.Entities.GuestEntity;
import com.vaibhav.StayNexus.Entities.UserEntity;
import com.vaibhav.StayNexus.Exceptions.ResourceNotFoundException;
import com.vaibhav.StayNexus.Exceptions.UnAuthorisedException;
import com.vaibhav.StayNexus.Repositories.GuestRepository;
import com.vaibhav.StayNexus.Service.Interfaces.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.vaibhav.StayNexus.Utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<GuestDTO> getAllGuests() {
        UserEntity currentUser = getCurrentUser();
        log.info("Getting all guests for user id: {}", currentUser.getId());

        return guestRepository.findByUser(currentUser)
                .stream()
                .map(guest -> modelMapper.map(guest, GuestDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public GuestDTO addNewGuest(GuestDTO guestDTO) {
        UserEntity currentUser = getCurrentUser();
        log.info("Adding new guest for user id: {}", currentUser.getId());

        GuestEntity guest = modelMapper.map(guestDTO, GuestEntity.class);
        guest.setUser(currentUser);
        guest = guestRepository.save(guest);

        return modelMapper.map(guest, GuestDTO.class);
    }

    @Override
    public void updateGuest(Long guestId, GuestDTO guestDto) {
        log.info("Updating guest id: {}", guestId);

        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guest not found with id: " + guestId));

        checkGuestOwnership(guest);

        modelMapper.map(guestDto , guest);
        guest.setId(guestId);
        guest.setUser(getCurrentUser());

        guestRepository.save(guest);
    }

    @Override
    public void deleteGuest(Long guestId) {
        log.info("Deleting guest id: {}", guestId);

        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Guest not found with id: " + guestId));

        checkGuestOwnership(guest);
        guestRepository.deleteById(guestId);
    }

    private void checkGuestOwnership(GuestEntity guest) {
        UserEntity currentUser = getCurrentUser();
        if(!currentUser.equals(guest.getUser())) {
            throw new UnAuthorisedException(
                    "Guest with id: " + guest.getId() + " does not belong to you"
            );
        }
    }

}






























