#!/bin/bash

echo "========================================"
echo "BTEU Schedule Backend Server"
echo "========================================"
echo ""

# Проверка наличия .env файла
if [ ! -f .env ]; then
    echo "[WARNING] .env file not found!"
    echo "Creating .env from example..."
    cp .env.example .env
    echo ""
    echo "Please edit .env file with your database settings!"
    echo ""
    read -p "Press enter to continue..."
fi

echo "Starting server..."
echo ""
python3 server.py

