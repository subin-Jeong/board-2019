package com.estsoft.domain;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Table(name="MEMBER_ROLE", uniqueConstraints = @UniqueConstraint(columnNames = {"MEMBER_ID", "ROLE_ID"}, name="MEMBER_ROLE_UNIQUE_MEMBER_ID_AND_ROLE_ID"))
public class MemberRole {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "bigint unsigned")
	private Integer id;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "MEMBER_ID", foreignKey = @ForeignKey(name = "FK_MEMBER_ROLE_MEMBER_ID"))
	private Member member;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "ROLE_ID", foreignKey = @ForeignKey(name = "FK_MEMBER_ROLE_ROLE_ID"))
	private Role role;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "MemberRole [id=" + id + ", member=" + member + ", role=" + role + "]";
	}
	
	
	
}
