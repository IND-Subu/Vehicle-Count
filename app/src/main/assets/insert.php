<?php
$username = "root";
$password = '';
$database = "asynch";
$server = "localhost";
$conn = mysqli_connect($server, $username, $password, $database);


// Create connection

// Check connection
if ($conn->connect_error) {
die("Connection failed: " . $conn->connect_error);
}
else{
echo "success";
}

if (isset($_POST['selectedValue'])) {
$selectedValue = $_POST['item'];

// Prepare the SQL statement
$sql = "INSERT INTO your_table (column_name) VALUES (?)";
$stmt = $conn->prepare($sql);

// Check if the statement is prepared successfully
if (!$stmt) {
die("Error in prepared statement: " . $conn->error);
}

// Bind the parameter and execute the statement
$stmt->bind_param("s", $selectedValue);

if ($stmt->execute()) {
echo "Data received and inserted successfully";
} else {
echo "Error executing prepared statement: " . $stmt->error;
}

// Close the statement
$stmt->close();
} else {
echo "Error: 'item' key not found in POST data";
}

// Close the connection
$conn->close();
?>
