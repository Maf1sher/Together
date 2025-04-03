package org.mafisher.togetherbackend.mappers.impl;

import lombok.AllArgsConstructor;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapperImpl implements Mapper<User, UserDto> {

    private ModelMapper modelMapper;

    @Override
    public UserDto mapTo(User userEntity) {
        return modelMapper.map(userEntity, UserDto.class);
    }


    @Override
    public User mapFrom(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
