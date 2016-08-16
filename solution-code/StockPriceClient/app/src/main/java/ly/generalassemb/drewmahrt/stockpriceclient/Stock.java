package ly.generalassemb.drewmahrt.stockpriceclient;

/**
 * Created by drewmahrt on 3/6/16.
 */
public class Stock {
    private String Name;
    private String Symbol;
    private double LastPrice;

    public String getName() {
        return Name;
    }

    public void setName(String stockName) {
        this.Name = stockName;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String stockSymbol) {
        this.Symbol = stockSymbol;
    }

    public double getLastPrice() {
        return LastPrice;
    }

    public void setLastPrice(double price) {
        this.LastPrice = price;
    }
}
