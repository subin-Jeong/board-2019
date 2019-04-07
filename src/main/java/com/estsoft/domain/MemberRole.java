package com.estsoft.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "MEMBER_ROLE")
public class MemberRole {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ROLE_NO")
	private Long roleNo;
	
	@Column(name = "ROLE_NAME")
	private String roleName;

	public Long getRoleNo() {
		return roleNo;
	}

	public void setRoleNo(Long roleNo) {
		this.roleNo = roleNo;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public String toString() {
		return "MemberRole [roleNo=" + roleNo + ", roleName=" + roleName + "]";
	}
	
	
}
