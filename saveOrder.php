<?php
    $con = mysqli_connect("deliveryrds.cqdp7841ahaa.ap-northeast-2.rds.amazonaws.com", "root", "!!insoo0128", "userclient");

    // POST로 받은 데이터 확인
    $userID = $_POST["userID"];
    $userAddress = $_POST["userAddress"];
    $shopName = $_POST["shopName"];
    $shopAddress = $_POST["shopAddress"];

    // 전달받은 변수들의 값을 로그로 출력
    error_log("Received userID: " . $userID);    
    error_log("Received userAddress: " . $userAddress);      
    error_log("Received shopName: " . $shopName); 
    error_log("Received shopAddress: " . $shopAddress);  

    $statement = mysqli_prepare($con, "INSERT INTO shopOrdering (userID, userAddress, shopName, shopAddress) VALUES (?, ?, ?, ?)");
    mysqli_stmt_bind_param($statement, "ssss", $userID, $userAddress, $shopName, $shopAddress);

    // 쿼리 실행 후 오류 체크
    if (!mysqli_stmt_execute($statement)) {
        error_log("SQL Error: " . mysqli_error($con));   // 로깅 추가
    }

    $response = array();
    $response["success"] = true;

    echo json_encode($response);
?>