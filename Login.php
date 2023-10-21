<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include('dbcon.php');

$userID = isset($_POST['userID']) ? $_POST['userID'] : '';
$userPassword = isset($_POST['userPassword']) ? $_POST['userPassword'] : '';
$failedLoginMessage = '';

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if ($userID != "") {

    // Use prepared statements to prevent SQL injection
    $sql = "SELECT * FROM user WHERE userID = :userID AND userPassword = SHA1(:userPassword)";
    $stmt = $con->prepare($sql);
    $stmt->bindParam(':userID', $userID);
    $stmt->bindParam(':userPassword', $userPassword);
    $stmt->execute();

    if ($stmt->rowCount() == 0) {
        if (!$android) {
            $failedLoginMessage = "'$userID' no id OR wrong password.";
        } else {
            echo "'$userID' no id OR wrong password.";
            exit;
        }
    } else {
        $data = array();

        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            extract($row);
            array_push($data, $row);
        }

        if (!$android) {
            print_r($data);
            exit;
        } else {
            header('Content-Type: application/json; charset=utf8');
            $json = json_encode(array("user" => $data), JSON_PRETTY_PRINT + JSON_UNESCAPED_UNICODE);
            echo $json;
            exit;
        }
    }
}

if (!$android) {
?>

<html>
<head>
    <meta charset="utf-8">
    <title>Login</title>
</head>
<body>
    <?php
    if($failedLoginMessage != '') {
        echo '<div style="color:red;">' . $failedLoginMessage . '</div>';
    }
    ?>
    <form action="<?php $_PHP_SELF ?>" method="POST">
        ID: <input type="text" name="userID">
        PASSWORD: <input type="text" name="userPassword">
        <input type="submit">
    </form>
</body>
</html>

<?php
}
?>