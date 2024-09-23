module ssi.master.descrypto {
    requires javafx.controls;
    requires javafx.fxml;


    opens ssi.master.descrypto to javafx.fxml;
    exports ssi.master.descrypto;
}