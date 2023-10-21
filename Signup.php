<?php
 
    error_reporting(E_ALL);
    ini_set('display_errors',1);
 
    include('dbcon.php');
 
    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
 
    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {
        $userID = $_POST['userID'];
        $userPassword = $_POST['userPassword'];
        $userAddress = $_POST['userAddress'];
        $phoneNumber = $_POST['phoneNumber'];
        
        // userSort값을 사용자 입력이 아닌 DB의 마지막 값에서 1 증가시킨 값으로 설정
        $stmt = $con->prepare("SELECT userSort FROM user ORDER BY userSort DESC LIMIT 1");
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $userSort = isset($row['userSort']) ? $row['userSort'] + 1 : 1; 

        if(empty($userID)) {
            $errMSG = "ID";
        } else if(empty($userPassword)) {
            $errMSG = "password";
        } else if(empty($userAddress)) {
            $errMSG = "userAddress";
        } else if(empty($phoneNumber)) {
            $errMSG = "phonenumber";
        }

        if(!isset($errMSG)) {
            // 중복된 ID 체크
            $checkID = $con->prepare("SELECT userID FROM user WHERE userID=:userID");
            $checkID->bindParam(':userID', $userID);
            $checkID->execute();

            if ($checkID->fetch(PDO::FETCH_ASSOC)) {
                $errMSG = "DUPLICATE_ID";
            } else {
                try {
                    $stmt = $con->prepare('INSERT INTO user(userID, userPassword, userAddress, phoneNumber, userSort) VALUES(:userID, SHA1(:userPassword), :userAddress, :phoneNumber, :userSort)');
                    $stmt->bindParam(':userID', $userID);
                    $stmt->bindParam(':userPassword', $userPassword);
                    $stmt->bindParam(':userAddress', $userAddress);
                    $stmt->bindParam(':phoneNumber', $phoneNumber);
                    $stmt->bindParam(':userSort', $userSort);

                    if($stmt->execute()) {
                        $successMSG = "SUCCESS";
                    } else {
                        $errMSG = "FAIL";
                    }
                } catch(PDOException $e) {
                    die("Database error: " . $e->getMessage());
                }
            }
        }
    }

    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;

    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
 
    if(!$android) {
?>
    <html>
       <body>
            <form action="<?php $_PHP_SELF ?>" method="POST">
                ID: <input type = "text" name = "userID" />
                Password: <input type = "text" name = "userPassword" />
                userAddress: <input type = "text" name = "userAddress" />
                phonenumber: <input type = "text" name = "phoneNumber" />
                <!-- usersort input을 제거하였습니다. -->
                <input type = "submit" name = "submit" />
            </form>
       </body>
    </html>
<?php
    }
?>