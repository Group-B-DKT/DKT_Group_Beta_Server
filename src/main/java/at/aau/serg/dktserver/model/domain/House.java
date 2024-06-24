package at.aau.serg.dktserver.model.domain;

public class House extends Building{
    private static final int HOUSE_PRICE = 200;
    private static final int MAX_AMOUNT = 4;

    public House(int price, int position) {
        super(price, position);
    }

    public static int getHousePrice(){
        return HOUSE_PRICE;
    }

    public int getMaxAmount(){
        return MAX_AMOUNT;
    }
}

