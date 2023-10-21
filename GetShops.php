<?php 
    $con = mysqli_connect("deliveryrds.cqdp7841ahaa.ap-northeast-2.rds.amazonaws.com", "root", "!!insoo0128", "userclient");

	ini_set('display_errors', 1);
	error_reporting(E_ALL);

    if (mysqli_connect_errno()) {
        die("Failed to connect to MySQL: " . mysqli_connect_error());
    }

    mysqli_set_charset($con,"utf8");

    $response = array();

    $result = mysqli_query($con, "SELECT * FROM shop");

    if ($result) {
        $response["shop"] = array();

        while ($row = mysqli_fetch_assoc($result)) {
            $shop = array();
            $shop["shopName"] = $row["shopName"];
            $shop["shopAddress"] = $row["shopAddress"];
            $shop["food"] = $row["food"];
            $shop["price"] = $row["price"];

            array_push($response["shop"], $shop);
        }

        header('Content-Type: application/json');
        echo json_encode($response);
    } else {
        echo "DB Error: " . mysqli_error($con);
    }

    mysqli_close($con);
?>