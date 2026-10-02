$GW = "http://localhost:9000"
$PASS = $true
$FAIL = $false

function Test-API($label, $method, $url, $body, $token, $expectedCode) {
    try {
        $headers = @{}
        if ($token) { $headers["Authorization"] = "Bearer $token" }
        if ($body) {
            $resp = Invoke-RestMethod -Uri $url -Method $method -ContentType "application/json" -Body $body -Headers $headers
        } else {
            $resp = Invoke-RestMethod -Uri $url -Method $method -Headers $headers
        }
        if ($expectedCode -and $expectedCode -ne 200 -and $expectedCode -ne 201 -and $expectedCode -ne 204) {
            Write-Host "[FAIL] $label - Expected HTTP $expectedCode but got 2xx" -ForegroundColor Red
        } else {
            Write-Host "[PASS] $label" -ForegroundColor Green
        }
        return $resp
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        if ($expectedCode -and $code -eq $expectedCode) {
            Write-Host "[PASS] $label (expected HTTP $code)" -ForegroundColor Green
            return $null
        }
        Write-Host "[FAIL] $label - HTTP $code : $($_.Exception.Message.Substring(0, [Math]::Min(60,$_.Exception.Message.Length)))" -ForegroundColor Red
        return $null
    }
}

Write-Host "`n========== INTEGRATION TEST ==========" -ForegroundColor Cyan

# ----- AUTH -----
Write-Host "`n--- [F1] AUTH ---" -ForegroundColor Yellow
$al = Test-API "F1.1: Admin Login via Gateway" POST "$GW/api/auth/login" '{"email":"admin@fucinema.com","password":"@@abc123@@"}'
$adminTok = $al.accessToken
if ($al) { Write-Host "  -> role=$($al.role) userId=$($al.userId)" }

$cl = Test-API "F1.2: Customer Login (an@gmail.com)" POST "$GW/api/auth/login" '{"email":"an@gmail.com","password":"123456"}'
$custTok = $cl.accessToken
if ($cl) { Write-Host "  -> role=$($cl.role) uid=$($cl.userId)" }

Test-API "F1.3: Wrong password -> 401" POST "$GW/api/auth/login" '{"email":"admin@fucinema.com","password":"wrongpass"}' $null $null 401

Test-API "F1.4: INACTIVE customer -> 403" POST "$GW/api/auth/login" '{"email":"chi@gmail.com","password":"123456"}' $null $null 403

# ----- CUSTOMER REGISTER & PROFILE -----
Write-Host "`n--- [F2] CUSTOMER SELF-SERVICE ---" -ForegroundColor Yellow
$newEmail = "testcust_$(Get-Random -Maximum 9999)@test.com"
$newBody = "{`"customerName`":`"Nguyen Test`",`"telephone`":`"0912345600`",`"email`":`"$newEmail`",`"customerBirthday`":`"2001-03-15`",`"password`":`"Abc@1234`"}"
$reg = Test-API "F2.1: Register new customer" POST "$GW/api/customers/register" $newBody
if ($reg) { Write-Host "  -> id=$($reg.customerId) email=$($reg.email)" }

Test-API "F2.2: Duplicate email -> 409" POST "$GW/api/customers/register" $newBody $null $null 409

if ($custTok) {
    $me = Test-API "F2.3: Get My Profile" GET "$GW/api/customers/me" $null $custTok
    if ($me) { Write-Host "  -> $($me.customerName) ($($me.customerStatus))" }

    $upd = Test-API "F2.4: Update My Profile" PUT "$GW/api/customers/me" '{"customerName":"Nguyen Van An Updated","telephone":"0905000001","customerBirthday":"2002-05-10"}' $custTok
    if ($upd) { Write-Host "  -> Updated name: $($upd.customerName)" }

    Test-API "F2.5: Wrong old password -> 400" PUT "$GW/api/customers/me/password" '{"oldPassword":"wrongpass","newPassword":"NewPass@999"}' $custTok $null 400
}

# ----- MOVIE SERVICE - GENRES -----
Write-Host "`n--- [F4] MOVIE SERVICE - GENRES ---" -ForegroundColor Yellow
$genres = Test-API "F4.1: List Genres (public)" GET "$GW/api/genres"
if ($genres) { Write-Host "  -> $($genres.Count) genres" }

if ($adminTok) {
    $randName = "GenreTest_$(Get-Random -Maximum 9999)"
    $newGenre = Test-API "F4.2: Admin Create Genre" POST "$GW/api/genres" "{`"genreName`":`"$randName`",`"description`":`"Test genre`"}" $adminTok
    if ($newGenre) {
        Write-Host "  -> created: $($newGenre.genreId) $($newGenre.genreName)"
        $gid = $newGenre.genreId
        $upG = Test-API "F4.3: Admin Update Genre" PUT "$GW/api/genres/$gid" "{`"genreName`":`"$randName Updated`",`"description`":`"Updated desc`"}" $adminTok
        if ($upG) { Write-Host "  -> updated: $($upG.genreName)" }
    }
    Test-API "F4.4: Customer cannot create genre -> 403" POST "$GW/api/genres" '{"genreName":"X","description":"X"}' $custTok $null 403
}

# ----- MOVIE SERVICE - ROOMS -----
Write-Host "`n--- [F5] CINEMA ROOMS ---" -ForegroundColor Yellow
$rooms = Test-API "F5.1: List Rooms (admin)" GET "$GW/api/rooms" $null $adminTok
if ($rooms) { Write-Host "  -> $($rooms.Count) rooms" }

if ($adminTok) {
    $randRoom = "TestRoom_$(Get-Random -Maximum 9999)"
    $newRoom = Test-API "F5.2: Admin Create Room" POST "$GW/api/rooms" "{`"roomName`":`"$randRoom`",`"roomType`":`"STANDARD`",`"seatRows`":6,`"seatsPerRow`":8,`"roomStatus`":`"ACTIVE`"}" $adminTok
    if ($newRoom) {
        Write-Host "  -> Room created: $($newRoom.roomId) $($newRoom.roomName)"
        $rid = $newRoom.roomId
        $upR = Test-API "F5.3: Admin Update Room" PUT "$GW/api/rooms/$rid" "{`"roomName`":`"$randRoom Updated`",`"roomType`":`"STANDARD`",`"seatRows`":6,`"seatsPerRow`":8,`"roomStatus`":`"ACTIVE`"}" $adminTok
        if ($upR) { Write-Host "  -> Updated: $($upR.roomName)" }
    }
}

# ----- MOVIE SERVICE - MOVIES -----
Write-Host "`n--- [F5] MOVIES ---" -ForegroundColor Yellow
$movies = Test-API "F5.4: List Movies (public)" GET "$GW/api/movies"
if ($movies) { Write-Host "  -> $($movies.Count) movies" }

if ($movies -and $movies.Count -gt 0) {
    $mid = $movies[0].movieId
    $movie = Test-API "F5.5: Get Movie Detail" GET "$GW/api/movies/$mid"
    if ($movie) { Write-Host "  -> $($movie.title) ($($movie.movieStatus))" }
}

# ----- SHOWTIMES -----
Write-Host "`n--- [F6] SHOWTIMES ---" -ForegroundColor Yellow
$showtimes = Test-API "F6.1: List Showtimes (public)" GET "$GW/api/showtimes"
if ($showtimes) { Write-Host "  -> $($showtimes.Count) showtimes total" }

$scheduled = $showtimes | Where-Object { $_.showtimeStatus -eq "SCHEDULED" } | Select-Object -First 1
if ($scheduled) {
    Write-Host "  Using: $($scheduled.showtimeId) - $($scheduled.movieTitle) @ $($scheduled.startTime)"
    $sid = $scheduled.showtimeId
    $stDetail = Test-API "F6.2: Get Showtime Detail" GET "$GW/api/showtimes/$sid"
    if ($stDetail) { Write-Host "  -> $($stDetail.seatRows)x$($stDetail.seatsPerRow) seats, price=$($stDetail.ticketPrice)" }
}

# Filter by date
$filterDate = $scheduled.startTime.Substring(0,10)
$filtST = Test-API "F6.3: Filter Showtimes by date" GET "$GW/api/showtimes?date=$filterDate"
if ($filtST) { Write-Host "  -> $($filtST.Count) showtimes on $filterDate" }

# ----- BOOKING -----
Write-Host "`n--- [F7] BOOKING ---" -ForegroundColor Yellow
if ($sid) {
    $seatMap = Test-API "F7.1: Seat Map (public)" GET "$GW/api/bookings/showtimes/$sid/seats"
    if ($seatMap) { Write-Host "  -> $($seatMap.totalSeats) total, $($seatMap.availableSeats) available, booked=$($seatMap.bookedSeats.Count)" }

    # Find 2 available seats
    if ($seatMap -and $custTok) {
        $allSeats = @()
        for ($r = 0; $r -lt $seatMap.seatRows; $r++) {
            for ($s = 1; $s -le $seatMap.seatsPerRow; $s++) {
                $allSeats += "$([char](65+$r))$s"
            }
        }
        $available = $allSeats | Where-Object { $_ -notin $seatMap.bookedSeats } | Select-Object -First 2
        if ($available.Count -ge 2) {
            $bookBody = "{`"items`":[{`"showtimeId`":`"$sid`",`"seatCode`":`"$($available[0])`"},{`"showtimeId`":`"$sid`",`"seatCode`":`"$($available[1])`"}]}"
            $booking = Test-API "F7.2: Create Booking (2 seats)" POST "$GW/api/bookings" $bookBody $custTok
            if ($booking) {
                Write-Host "  -> bookingId=$($booking.bookingId) total=$($booking.totalPrice) status=$($booking.bookingStatus)"
                $bookId = $booking.bookingId
            }
            # Duplicate seat in same request -> 400
            $dupeBody = "{`"items`":[{`"showtimeId`":`"$sid`",`"seatCode`":`"$($available[0])`"},{`"showtimeId`":`"$sid`",`"seatCode`":`"$($available[0])`"}]}"
            Test-API "F7.3: Duplicate seat in request -> 400" POST "$GW/api/bookings" $dupeBody $custTok $null 400

            # Already booked seat -> 409
            $takenBody = "{`"items`":[{`"showtimeId`":`"$sid`",`"seatCode`":`"$($available[0])`"}]}"
            Test-API "F7.4: Already-booked seat -> 409" POST "$GW/api/bookings" $takenBody $custTok $null 409
        }
    }
}

# ----- BOOKING HISTORY & CANCEL -----
Write-Host "`n--- [F8] BOOKING HISTORY ---" -ForegroundColor Yellow
if ($custTok) {
    $myBookings = Test-API "F8.1: My Booking History" GET "$GW/api/bookings/my" $null $custTok
    if ($myBookings) { Write-Host "  -> $($myBookings.Count) booking(s)" }

    if ($bookId -and $myBookings) {
        $detail = Test-API "F8.2: Get Booking by ID" GET "$GW/api/bookings/$bookId" $null $custTok
        if ($detail) { Write-Host "  -> Booking $bookId status=$($detail.bookingStatus)" }
    }
}

if ($adminTok) {
    $allBookings = Test-API "F8.3: Admin List All Bookings" GET "$GW/api/bookings" $null $adminTok
    if ($allBookings) { Write-Host "  -> $($allBookings.Count) total bookings" }
}

# ----- REPORT -----
Write-Host "`n--- [F9] REVENUE REPORT ---" -ForegroundColor Yellow
if ($adminTok) {
    $report = Test-API "F9.1: Revenue Report" GET "$GW/api/bookings/report?startDate=2026-01-01&endDate=2026-12-31" $null $adminTok
    if ($report) {
        Write-Host "  -> Bookings=$($report.totalBookings), Tickets=$($report.totalTicketsSold), Revenue=$($report.totalRevenue)"
        Write-Host "  -> Revenue by movie:"
        foreach ($mv in $report.revenueByMovie) {
            Write-Host "     $($mv.movieTitle): $($mv.ticketsSold) tickets, $($mv.revenue) VND"
        }
    }
    Test-API "F9.2: Invalid date range -> 400" GET "$GW/api/bookings/report?startDate=2026-12-31&endDate=2026-01-01" $null $adminTok $null 400
}

# ----- ADMIN CUSTOMER CRUD -----
Write-Host "`n--- [F3] ADMIN CUSTOMER CRUD ---" -ForegroundColor Yellow
if ($adminTok) {
    $allCusts = Test-API "F3.1: Admin List Customers" GET "$GW/api/customers" $null $adminTok
    if ($allCusts) { Write-Host "  -> $($allCusts.Count) customers" }

    $search = Test-API "F3.2: Admin Search Customers (keyword=an)" GET "$GW/api/customers?keyword=an" $null $adminTok
    if ($search) { Write-Host "  -> $($search.Count) results for 'an'" }
}

Write-Host "`n========== ALL TESTS DONE ==========" -ForegroundColor Cyan
