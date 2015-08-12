package com.sample.sandbox;

public class Address {

    private String town;

    
    
    public Address() {
    }

    public Address(String town) {
        this.town = town;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((town == null) ? 0 : town.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (town == null) {
            if (other.town != null)
                return false;
        } else if (!town.equals(other.town))
            return false;
        return true;
    }
    
    
}
