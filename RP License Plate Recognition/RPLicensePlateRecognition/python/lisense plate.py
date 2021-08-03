import numpy as np
import cv2
import imutils
import easyocr
from matplotlib import pyplot as plt
from PIL import Image
#import pytesseract as tess



def CheckPlate(imgN):
    #imgN = input("name of the image: ")
    Staff = ["MR. Tan","MS.Jac", "MR.Vincent Ng","MS.Bee","MS.Sharmila"]
    cars = ["SG55X", "SLK6291A", "SGD6789B", "SGS1234G", "SZ1233T"]
    Email = ["tan@live.sg", "jac@outlook.com", "vincent@yahoo.com", "b123@gmail.com", "sharmila@gmail.com"]

    img = cv2.imread(imgN + ".jpg")
    img = cv2.resize(img, (1000,800) )
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    #plt.imshow(cv2.cvtColor(gray, cv2.COLOR_BGR2RGB))

    bfilter = cv2.bilateralFilter(gray, 11, 17, 17) #Noise reduction
    edged = cv2.Canny(bfilter, 30, 200) #Edge detection
    #plt.imshow(cv2.cvtColor(edged, cv2.COLOR_BGR2RGB))


    keypoints = cv2.findContours(edged.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contours = imutils.grab_contours(keypoints)
    contours = sorted(contours, key=cv2.contourArea, reverse=True)[:10]


    location = None
    for contour in contours:
        approx = cv2.approxPolyDP(contour, 10 , True)
        if len(approx) == 4:
            location = approx
            break

    if location is None:
        detected = 0
        print ("No contour detected")
    else:
        detected = 1

    if detected == 1:
        #cv2.drawContours(img, [location], -1, (0, 0, 255), 3)
        mask = np.zeros(gray.shape, np.uint8)
        new_image = cv2.drawContours(mask, [location], 0,255, -1,)
        new_image = cv2.bitwise_and(img, img, mask=mask)

        (x,y) = np.where(mask==255)
        (x1, y1) = (np.min(x), np.min(y))
        (x2, y2) = (np.max(x), np.max(y))
        cropped_image = gray[x1:x2+1, y1:y2+1]


        #plt.imshow(cv2.cvtColor(cropped_image, cv2.COLOR_BGR2RGB))


        reader = easyocr.Reader(['en'])
        result = reader.readtext(cropped_image)
        result
        print(result)




        #text = result[0][-2]
    #font = cv2.FONT_HERSHEY_SIMPLEX
    #res = cv2.putText(img, text=text, org=(approx[0][0][0], approx[1][0][1]+60), fontFace=font, fontScale=1, color=(0,255,0), thickness=2, lineType=cv2.LINE_AA)
    #res = cv2.rectangle(img, tuple(approx[0][0]), tuple(approx[2][0]), (0,255,0),3)
    #plt.imshow(cv2.cvtColor(res, cv2.COLOR_BGR2RGB))

    #print("===============================================================")
    #print("the license plate is : " + text)
    #checktext = input("is this correct (y/n): ")

    #if checktext == "n" or checktext =="N":
    #redo = input("please input correct license plate: ")
    #text = redo


    #m = int(0)
    #user = str(text)
    #while m == 0:
    #for x in range(len(cars)):
    #if cars[x] == user.upper():
    #txt = "Name:    {}".format(Staff[x])
    #txt2 = "Lisence: {}".format(cars[x])
    #txt3 = "Email: {}".format(Email[x])
    #print(txt)
    #print(txt2)
    #print(txt3)
    #print("===============================================================")
    #break
    #else:
    #print("user not found")
    #print("===============================================================")
    #break
    #break

