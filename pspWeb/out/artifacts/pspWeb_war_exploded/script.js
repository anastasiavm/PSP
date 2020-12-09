let maxSize = 900;

let btnSetFilter;

let ws;

let image;

window.onload = function () {

    ws = new WebSocket("ws://localhost:8083/hello");

    ws.onopen = function() {
        console.log("connection opened");
    };

    ws.onclose = function() {
        console.log("connection closed");
    };

    ws.onerror = function wserror(message) {
        console.log("error: " + message);
    }

    //
    ws.onmessage = function(message) {
        setModifiedCanvas(strToArray(message.data));
    }

    btnSetFilter = document.querySelector("#btn-set-filter");

    btnSetFilter.onclick = function() {
        // Матрица пикселей
        let pixelsMatrix = getStockCanvasPixelsMatrix();

        // Отправка на веб-сокет. Отправить данные нужно будет в формате JSON
        let data = {
            filterSize: document.querySelector("#filter-size").value,
            pixels: pixelsMatrix
        }

        //console.log(matrix);
        //console.log(typeof (matrix));
        // let data = {
        //     filterSize: document.querySelector("#filter-size").value,
        //     imageBase: pixelsMatrix
        // }
        // toBase64(image)
        //     .then(result => {
        //         data["imageBase"] = result[0]
        //
        //     });
        ws.send(JSON.stringify(data));
    };

    document.querySelector("#img_input").onchange = function (e) {
        btnSetFilter.setAttribute("disabled", "disabled");
        // Это пример загрузки файлов
        // result - это то, что возвращает Promise (массив из dataURL)
        // В console.log выводятся матрицы пикселей
        imgLoader(e.target.files)
            .then(result => {
                console.log(result[0]);
                setStockCanvas(result[0])
                    .then(r => {
                        btnSetFilter.removeAttribute("disabled");
                    })
            });

        // file
        // image = e.target.files[0]
        // toBase64(image)
        //     .then(result => {
        //         console.log(result);
        //         setStockCanvas(result)
        //             .then(r => {
        //                 btnSetFilter.removeAttribute("disabled");
        //             })
        //     });
    }
};

const toBase64 = file => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
});

/**
 * Отрисовывает исходную картинку
 * @param dataURL
 * @returns {Promise<unknown>}
 */
function setStockCanvas(dataURL) {
    let container = document.querySelector("#stock-image-container");
    if (container.children.length !== 0) {
        container.removeChild(container.children[0]);
    }

    let canvas = document.createElement("canvas");
    canvas.id = "stock-img";

    container.appendChild(canvas);

    let ctx = canvas.getContext("2d");

    return new Promise(((resolve) => {
        let img = new Image();
        img.onload = function () {
            let ch = img.height;
            let cw = img.width;

            let threshold = 1;

            if (cw > maxSize) {
                threshold = cw / maxSize;
            } else if (ch > maxSize) {
                threshold = ch / maxSize;
            }

            console.log(threshold);

            ch /= threshold;
            cw /= threshold;

            canvas.width = cw;
            canvas.height = ch;

            ctx.clearRect(0, 0, cw, ch);
            ctx.drawImage(img, 0, 0, cw, ch);

            resolve(true);
        };

        img.src = dataURL;
    }));
}

/**
 * Отрисовывает модифицированную картинку
 * @param pixelMatrix матрица пикселей [ [ [R, G, B] ] ]
 */
function setModifiedCanvas(pixelMatrix) {
    let container = document.querySelector("#modified-image-container");
    if (container.children.length !== 0) {
        container.removeChild(container.children[0]);
    }

    let canvas = document.createElement("canvas");
    canvas.id = "modified-img";

    container.appendChild(canvas);

    let ctx = canvas.getContext("2d");

    let width = document.querySelector("#stock-img").clientWidth;
    let height = document.querySelector("#stock-img").clientHeight;

    canvas.width = width;
    canvas.height = height;

    let img = ctx.createImageData(width, height);

    let count = 0;
    let isAlpha = 0;
    for (let i = 0; i < pixelMatrix.length; i++) {
        img.data[count] = pixelMatrix[i];
        count += 1;
        isAlpha += 1;
        if (isAlpha === 3) {
            img.data[count] = 255;
            count +=1;
            isAlpha = 0;
        }
    }

    /*
    let count = 0;
    for (let i = 0; i < pixelMatrix.length; i++) {
        for (let j = 0; j < pixelMatrix[i].length; j++) {
            for (let k = 0; k < 3; k++) {
                img.data[count] = pixelMatrix[i][j][k];
                count += 1;
            }
            img.data[count] = 255;
            count += 1;
        }
    }*/

    ctx.putImageData(img, 0, 0);
}

/**
 * Возвращает матрицу пикселей исходной картинки
 * @returns {[]}
 */
function getStockCanvasPixelsMatrix() {
    let canvas = document.querySelector("#stock-img");
    let ctx = canvas.getContext("2d");
    let width = canvas.clientWidth;
    let height = canvas.clientHeight;

    let imgData = ctx.getImageData(0, 0, width, height).data;

    let pixMatrix = [];

    let count = 0;
    for (let i = 0; i < height; i++) {
        let row = [];
        for (let j = 0; j < width; j++) {
            let pix = [];
            for (let k = 0; k < 3; k++) {
                pix.push(imgData[count]);
                count += 1;
            }
            count += 1;
            row.push(pix);
        }
        pixMatrix.push(row);
    }

    return pixMatrix;
}

/**
 * Загружает картинки (а именно их dataURL)
 * Эта фукнция служит обработчиком события onchange для input type=file
 * @param files Файлы с картинками
 * @returns {Promise|null} Promise.
 * В случае удачной загрузки файла Promise будет содержать массив из dataURL
 */
function imgLoader(files) {
    if (FileReader && files && files.length) {
        let dataURLs = [];
        let ready = 0;

        return new Promise((resolve, reject) => {
            for (let i = 0; i < files.length; i++) {
                let fr = new FileReader();

                fr.onload = function () {
                    dataURLs.push(fr.result);
                    ready += 1;

                    if (ready === files.length) {
                        resolve(dataURLs);
                    }
                };

                fr.readAsDataURL(files[i]);
            }
        });
    } else {
        return null;
    }
}

function strToArray(str) {
    let arr = [];
    let values = str.replace("[", "").replace("]", "").split(",");

    for (let i = 0; i < values.length; i++) {
        arr.push(Number(values[i]));
    }

    return arr;
}
