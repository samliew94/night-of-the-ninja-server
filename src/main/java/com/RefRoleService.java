package com;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class RefRoleService {
	
	@Autowired
	RefRolesRepository refRolesRepository;
	
	@PostConstruct
	void initialization() {
		
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.FORENSIC.getValue()).roleName("Forensic Scientist").isEnabled(true).build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.MURDERER.getValue()).roleName("Murderer").isEnabled(true).build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.INVESTIGATOR.getValue()).roleName("Investigator").isEnabled(true).build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.ACCOMPLICE.getValue()).roleName("Accomplice").isEnabled(false).build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.WITNESS.getValue()).roleName("Witness").isEnabled(false).build());
			
	}

	/**
	 * @return null if not found
	 */
	public RefRole findByGameRole(GameRoles gameRole) {
		// TODO Auto-generated method stub
		return refRolesRepository.findByRoleCode(gameRole.getValue());
	}

	public List<RefRole> findAllByGameRolesIn(GameRoles ... gameRoles) {
		
		return refRolesRepository.findAllByRoleCodeIn(Arrays.asList(gameRoles).stream().map(x->x.getValue()).collect(Collectors.toList()));
	}
	
	public List<RefRole> findAllByRoleCodeNotIn(GameRoles ...gameRoles) {
		
		return refRolesRepository.findAllByRoleCodeNotIn(Arrays.asList(gameRoles).stream().map(x->x.getValue()).collect(Collectors.toList()));
	}

	public void toggleOptionalRoles() throws Exception {
		
		RefRole accomplice = findByGameRole(GameRoles.ACCOMPLICE);
		RefRole witness = findByGameRole(GameRoles.WITNESS);
		
		accomplice.setEnabled(!accomplice.isEnabled());
		witness.setEnabled(!witness.isEnabled());
		
	}

	public boolean areOptionalRolesEnabled() {
		
		RefRole accomplice = findByGameRole(GameRoles.ACCOMPLICE);
		RefRole witness = findByGameRole(GameRoles.WITNESS);
		
		return accomplice.isEnabled() && witness.isEnabled();
		
	}

	
}