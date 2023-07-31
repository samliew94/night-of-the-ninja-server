package com;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class UserRoleService {
	
	@Autowired
	UserRolesRepository userRolesRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	RefRoleService refRoleService;
	
	public void reset() {
		
		userRolesRepository.deleteAll();
		
		for(MyUser user : userService.findAll())
			insert(user, user.isHost() ? GameRoles.FORENSIC : null);
		
	}
	
	public void hardReset() {
		
		userRolesRepository.deleteAll();
		
	}
	
	public void insert(MyUser user, GameRoles gameRole) {
		
		UserRoles userRoles = UserRoles.builder()
			.user(user)
			.refRole(gameRole == null ? null : refRoleService.findByGameRole(gameRole))
			.build()
			;
		
		userRolesRepository.save(userRoles);
	}
	
	public void onSelectedForensic(Map body) {
		
		String newForensicUsername = (String) body.get("username");
		MyUser newForensic = userService.findByUsername(newForensicUsername);
		
		UserRoles userRoleA = findByRole(GameRoles.FORENSIC);
		UserRoles userRoleB = findByUser(newForensic);
		
		RefRole refRoleA = userRoleA.getRefRole();
		RefRole refRoleB = userRoleB.getRefRole();
		
		userRoleA.setRefRole(refRoleB);
		userRoleB.setRefRole(refRoleA);
		
		userRolesRepository.save(userRoleA);
		userRolesRepository.save(userRoleB);
		
		System.err.println(userRoleA.getUser().getUsername() + " is now " + (userRoleA.getRefRole() == null ? "-" : userRoleA.getRefRole().getRoleName()));
		System.err.println(userRoleB.getUser().getUsername() + " is now " + (userRoleB.getRefRole() == null ? "-" : userRoleB.getRefRole().getRoleName()));
		
	}


	public UserRoles findByUser(MyUser user) {
		// TODO Auto-generated method stub
		return userRolesRepository.findByUser(user);
	}

	public UserRoles findByRole(GameRoles gameRole) {
		RefRole refRole = refRoleService.findByGameRole(gameRole);
		return userRolesRepository.findByRefRole(refRole);
	}
	
	public void updateUserRole(UserRoles userRole, GameRoles role) {
		// TODO Auto-generated method stub
		RefRole refRole = refRoleService.findByGameRole(role);
		userRole.setRefRole(refRole);
		userRolesRepository.save(userRole);
	}

	/**
	 * @return true if user's role matches with ANY of GameRole(s)
	 */
	public boolean compare(MyUser user, GameRoles ...roles ) {
		
		UserRoles userRole = userRolesRepository.findByUser(user);
		
		for (GameRoles role : roles)
			if (userRole.getRefRole().getRoleCode().equalsIgnoreCase(role.getValue()))
				return true;
		
		return false;
		
		
	}

	public List<UserRoles> findAll() {
		// TODO Auto-generated method stub
		return userRolesRepository.findAll();
	}
	
	public List<UserRoles> findAllByGameRoles(GameRoles ...gameRole) {
		
		List<RefRole> refRoles = refRoleService.findAllByGameRolesIn(gameRole);;
		
		return userRolesRepository.findAllByRefRoleIn(refRoles);
		
	}
	
	public boolean userIsForensic(MyUser user) {
		
		UserRoles userRole = userRolesRepository.findByUser(user);
		
		RefRole refRole = userRole.getRefRole();
		
		return refRole != null && refRole.getRoleCode().equals(GameRoles.FORENSIC.getValue());
		
	}
	
	public boolean userIsMurderer(MyUser user) {
		
		return compare(user, GameRoles.MURDERER);
		
	}
	
	public boolean userIsInvestigator(MyUser user) {
		
		return compare(user, GameRoles.INVESTIGATOR);
		
	}
	
	public boolean userIsAccomplice(MyUser user) {
		
		return compare(user, GameRoles.ACCOMPLICE);
		
	}
	
	public boolean userIsWitness(MyUser user) {
		
		return compare(user, GameRoles.WITNESS);
		
	}

	public UserRoles findForensic() {
		// TODO Auto-generated method stub
		
		RefRole refRole = refRoleService.findByGameRole(GameRoles.FORENSIC);
		
		return userRolesRepository.findByRefRole(refRole);
		
	}
	
	public UserRoles findMurderer() {
		
		RefRole refRole = refRoleService.findByGameRole(GameRoles.MURDERER);
		return userRolesRepository.findByRefRole(refRole);
		
	}

	public List<UserRoles> findAllExceptForensic() {
		
		List<UserRoles> all = userRolesRepository.findAll();
		all.remove(findForensic());
		
		return all;
	}

	public List<UserRoles> findAllWithoutRoles() {
		
		List<UserRoles> all = findAll();
		all.removeIf(x->x.getRefRole() != null);
		return all;
		
	}

	public GameRoles findGameRoleByUser(MyUser user) {
		// TODO Auto-generated method stub
		RefRole refRole = findByUser(user).getRefRole();
		
		String roleCode = refRole.getRoleCode();
		
		return GameRoles.valueOf(roleCode);
		
	}

}