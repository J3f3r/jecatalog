package com.jeferson.jecatalog.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeferson.jecatalog.dto.RoleDTO;
import com.jeferson.jecatalog.dto.UserDTO;
import com.jeferson.jecatalog.dto.UserInsertDTO;
import com.jeferson.jecatalog.dto.UserUpdateDTO;
import com.jeferson.jecatalog.entities.Role;
import com.jeferson.jecatalog.entities.User;
import com.jeferson.jecatalog.projection.UserDetailsProjection;
import com.jeferson.jecatalog.repositories.RoleRepository;
import com.jeferson.jecatalog.repositories.UserRepository;
import com.jeferson.jecatalog.services.exceptions.DatabaseException;
import com.jeferson.jecatalog.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService implements UserDetailsService{

	@Autowired
	private UserRepository repository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	BCryptPasswordEncoder passordEncoder;
	
	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageable){
		Page<User> list = repository.findAll(pageable);
		return list.map(x -> new UserDTO(x));
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional <User> obj = repository.findById(id);
		User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO insert(UserInsertDTO dto) {// refatorado para receber o passord somente neste endpoint
		User entity = new User();
		copyDtoToEntity(dto, entity);
		
		entity.setPassword(passordEncoder.encode(dto.getPassword()));
		// linha adicionada para pegar a senha
		entity = repository.save(entity);
		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO update(Long id, UserUpdateDTO dto) {
		try {
			User entity = repository.getReferenceById(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new UserDTO(entity);
		}
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id noy found " + id);
		}
	}

	public void delete(Long id) {
		if( !repository.existsById(id)) {
			throw new ResourceNotFoundException("Resource not found");
		}
		try {
			repository.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	private void copyDtoToEntity(UserDTO dto, User entity) {
		// metodo chamado para evitar escrita desnecessaria
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());
		
		
		entity.getRoles().clear();
		
		for(RoleDTO roleDto : dto.getRoles()) {
			Role role = roleRepository.getReferenceById(roleDto.getId());// NAO vai tocar no banco de dados
			entity.getRoles().add(role);
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		// result representa a lista dessa classe e recebe a lista de roles do email no banco de dados
//				List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
//				
//				if(result.size() == 0) {
//					throw new UsernameNotFoundException("User not found!");
//				}// result nao tiver nada retorna a exception
//				
//				User user = new User();
//				user.setEmail(username);
//				user.setPassword(result.get(0).getPassword());
//				// acessa o primeiro elemento, pega sua senha no banco e salva no user
//				
//				// agora vamos pindurar as roles nesse usuario
//				for(UserDetailsProjection projection : result) {
//					user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
//				}
//				
//				return user;
		List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
		if (result.isEmpty()) {
		    throw new UsernameNotFoundException("User not found!");
		}

		// Criamos o objeto User do Spring Security usando os dados da projeção
		return new org.springframework.security.core.userdetails.User(
		    result.get(0).getUsername(), 
		    result.get(0).getPassword(), 
		    true, true, true, true, 
		    result.stream().map(x -> new SimpleGrantedAuthority(x.getAuthority())).toList()
		);
	}
}
