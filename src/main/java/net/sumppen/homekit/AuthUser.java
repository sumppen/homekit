package net.sumppen.homekit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="AuthUser")
public class AuthUser {
	@Id @GeneratedValue
	@Column(name = "id")
	private int id;
	@Column(name = "name")
	private String userName;
	@Column(name = "key")
	private byte[] publicKey;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public byte[] getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	} 
}
