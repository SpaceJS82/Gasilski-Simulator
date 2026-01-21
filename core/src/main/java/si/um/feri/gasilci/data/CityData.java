package si.um.feri.gasilci.data;

public class CityData {
    public String name;
    public double lat;
    public double lon;

    public CityData() {
    }

    public CityData(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return name;
    }
}
